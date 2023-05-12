package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.disk.DiskUsageBytesGauge;
import org.jenkinsci.plugins.prometheus.collectors.disk.FileStoreAvailableGauge;
import org.jenkinsci.plugins.prometheus.collectors.disk.FileStoreCapacityGauge;
import org.jenkinsci.plugins.prometheus.collectors.disk.JobUsageBytesGauge;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.*;

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
            LOGGER.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not loaded.");
            return Collections.emptyList();
        }

        final List<MetricFamilySamples> samples = new ArrayList<>();
        final Set<FileStore> usedFileStores = new HashSet<>();

        final String namespace = ConfigurationUtils.getNamespace();
        final String subSystem = ConfigurationUtils.getSubSystem();

        final DiskUsageBytesGauge directoryUsageGauge = new DiskUsageBytesGauge(new String[]{"file_store", "directory"}, namespace, subSystem);
        diskUsagePlugin.getDirectoriesUsages().forEach(i -> {
            final Optional<FileStore> fileStore = getFileStore(i.getPath());
            fileStore.ifPresent(usedFileStores::add);
            directoryUsageGauge.calculateMetric(i, new String[]{toLabelValue(fileStore), i.getDisplayName()});
        });
        samples.addAll(directoryUsageGauge.collect());

        final JobUsageBytesGauge jobUsageGauge = new JobUsageBytesGauge(new String[]{"file_store", "jobName", "url"}, namespace, subSystem);
        diskUsagePlugin.getJobsUsages().forEach(i -> {
            final Optional<FileStore> fileStore = getFileStore(i.getPath());
            fileStore.ifPresent(usedFileStores::add);
            jobUsageGauge.calculateMetric(i, new String[]{toLabelValue(fileStore), i.getFullName(), i.getUrl()});
        });
        samples.addAll(jobUsageGauge.collect());

        final FileStoreCapacityGauge fileStoreCapacityGauge = new FileStoreCapacityGauge(new String[]{"file_store"}, namespace, subSystem);
        final FileStoreAvailableGauge fileStoreAvailableGauge = new FileStoreAvailableGauge(new String[]{"file_store"}, namespace, subSystem);
        usedFileStores.forEach(store -> {
            final String labelValue = toLabelValue(Optional.of(store));
            fileStoreCapacityGauge.calculateMetric(store, new String[]{labelValue});
            fileStoreAvailableGauge.calculateMetric(store, new String[]{labelValue});
        });
        samples.addAll(fileStoreCapacityGauge.collect());
        samples.addAll(fileStoreAvailableGauge.collect());

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
