package org.jenkinsci.plugins.prometheus.collectors;

import com.cloudbees.simplediskusage.DiskItem;
import com.cloudbees.simplediskusage.JobDiskItem;
import hudson.model.Job;
import hudson.model.LoadStatistics;
import hudson.model.Run;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.disk.DiskCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.executors.ExecutorCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.jenkins.JenkinsCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.jobs.JobCollectorFactory;

import java.nio.file.FileStore;

public class CollectorFactory {

    private final BuildCollectorFactory buildCollectorFactory;
    private final JobCollectorFactory jobCollectorFactory;
    private final JenkinsCollectorFactory jenkinsCollectorFactory;
    private final ExecutorCollectorFactory executorCollectorFactory;
    private final DiskCollectorFactory diskCollectorFactory;

    public CollectorFactory() {
        buildCollectorFactory = new BuildCollectorFactory();
        jobCollectorFactory = new JobCollectorFactory();
        jenkinsCollectorFactory = new JenkinsCollectorFactory();
        executorCollectorFactory = new ExecutorCollectorFactory();
        diskCollectorFactory = new DiskCollectorFactory();
    }

    public MetricCollector<Run, ? extends Collector> createRunCollector(CollectorType type, String[] labelNames, String prefix) {
        return buildCollectorFactory.createCollector(type, labelNames, prefix);
    }

    public MetricCollector<Job, ? extends Collector> createJobCollector(CollectorType type, String[] labelNames) {
        return jobCollectorFactory.createCollector(type, labelNames);
    }

    public MetricCollector<Jenkins, ? extends Collector> createJenkinsCollector(CollectorType type, String[] labelNames) {
        return jenkinsCollectorFactory.createCollector(type, labelNames);
    }

    public MetricCollector<LoadStatistics.LoadStatisticsSnapshot, ? extends Collector> createExecutorCollector(CollectorType type, String[] labelNames, String prefix) {
        return executorCollectorFactory.createCollector(type, labelNames, prefix);
    }

    public MetricCollector<DiskItem, ? extends Collector> createDiskItemCollector(CollectorType type, String[] labelNames) {
        return diskCollectorFactory.createDiskItemCollector(type, labelNames);
    }

    public MetricCollector<JobDiskItem, ? extends Collector> createJobDiskItemCollector(CollectorType type, String[] labelNames) {
        return diskCollectorFactory.createJobDiskItemCollector(type, labelNames);
    }

    public MetricCollector<FileStore, ? extends Collector> createFileStoreCollector(CollectorType type, String[] labelNames) {
        return diskCollectorFactory.createFileStoreCollector(type, labelNames);
    }
}
