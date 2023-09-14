package org.jenkinsci.plugins.prometheus;

import com.cloudbees.simplediskusage.DiskItem;
import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiskUsageCollector extends Collector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUsageCollector.class);

    @Override
    @Nonnull
    public List<MetricFamilySamples> collect() {

        if (!PrometheusConfiguration.get().getCollectDiskUsage()) {
            return Collections.emptyList();
        }

        try {
            return collectDiskUsage();
        } catch (final IOException | RuntimeException e) {
            LOGGER.warn("Failed to get disk usage data due to an unexpected error.", e);
            return Collections.emptyList();
        } catch (final java.lang.NoClassDefFoundError e) {
            LOGGER.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not installed: {}", e.toString());
            return Collections.emptyList();
        }
    }

    @Nonnull
    private static List<MetricFamilySamples> collectDiskUsage() throws IOException {
        final com.cloudbees.simplediskusage.QuickDiskUsagePlugin diskUsagePlugin = Jenkins.get()
                .getPlugin(com.cloudbees.simplediskusage.QuickDiskUsagePlugin.class);
        if (diskUsagePlugin == null) {
            return Collections.emptyList();
        }

        CollectorFactory factory = new CollectorFactory();
        final Set<FileStore> usedFileStores = new HashSet<>();
        List<MetricCollector<DiskItem, ? extends Collector>> diskItemCollectors = new ArrayList<>();
        diskItemCollectors.add(factory.createDiskItemCollector(CollectorType.DISK_USAGE_BYTES_GAUGE, new String[]{"file_store", "directory"}));

        diskUsagePlugin.getDirectoriesUsages().forEach(i -> {
            final Optional<FileStore> fileStore = getFileStore(i.getPath());
            fileStore.ifPresent(usedFileStores::add);
            diskItemCollectors.forEach(c -> c.calculateMetric(i, new String[]{toLabelValue(fileStore), i.getDisplayName()}));
        });

        List<MetricCollector<JobDiskItem, ? extends Collector>> jobDiskItemCollectors = new ArrayList<>();
        jobDiskItemCollectors.add(factory.createJobDiskItemCollector(CollectorType.JOB_USAGE_BYTES_GAUGE, new String[]{"file_store", "jobName", "url"}));

        diskUsagePlugin.getJobsUsages().forEach(i -> {
            final Optional<FileStore> fileStore = getFileStore(i.getPath());
            fileStore.ifPresent(usedFileStores::add);
            jobDiskItemCollectors.forEach(c -> c.calculateMetric(i, new String[]{toLabelValue(fileStore), i.getFullName(), i.getUrl()}));
        });

        List<MetricCollector<FileStore, ? extends Collector>> fileStoreCollectors = new ArrayList<>();
        fileStoreCollectors.add(factory.createFileStoreCollector(CollectorType.FILE_STORE_CAPACITY_GAUGE, new String[]{"file_store"}));
        fileStoreCollectors.add(factory.createFileStoreCollector(CollectorType.FILE_STORE_AVAILABLE_GAUGE, new String[]{"file_store"}));

        usedFileStores.forEach(store -> {
            final String labelValue = toLabelValue(Optional.of(store));
            fileStoreCollectors.forEach(c -> c.calculateMetric(store, new String[]{labelValue}));
        });

        List<MetricFamilySamples> samples = new ArrayList<>();

        samples.addAll(Stream.of(diskItemCollectors)
                .flatMap(Collection::stream)
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        samples.addAll(Stream.of(jobDiskItemCollectors)
                .flatMap(Collection::stream)
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        samples.addAll(Stream.of(fileStoreCollectors)
                .flatMap(Collection::stream)
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        return samples;
    }

    private static String toLabelValue(Optional<FileStore> fileStore) {
        // At least on Linux, FileStore::name is not unique, whereas FileStore::toString includes the mount point, which
        // makes it unique. So it's possible to have duplicate metrics with different label values for the same file
        // store mounted to different paths.
        return fileStore.map(FileStore::toString).orElse("<unknown>");
    }

    private static Optional<FileStore> getFileStore(File file) {
        try {
            return Optional.of(Files.getFileStore(file.toPath().toRealPath()));
        } catch (IOException | RuntimeException e) {
            LOGGER.debug("Failed to get file store for {}", file, e);
            return Optional.empty();
        }
    }
}
