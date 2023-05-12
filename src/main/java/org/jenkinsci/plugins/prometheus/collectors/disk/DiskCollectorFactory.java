package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import java.nio.file.FileStore;
import java.util.Objects;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class DiskCollectorFactory extends BaseCollectorFactory {

    public DiskCollectorFactory() {
        super();
    }

    public MetricCollector<DiskItem, ? extends Collector> createDiskItemCollector(CollectorType type, String[] labelNames) {
        if (Objects.requireNonNull(type) == DISK_USAGE_BYTES_GAUGE) {
            return isEnabledViaConfig(DISK_USAGE_BYTES_GAUGE) ? new DiskUsageBytesGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
        }
        return new NoOpMetricCollector<>();
    }

    public MetricCollector<JobDiskItem, ? extends Collector> createJobDiskItemCollector(CollectorType type, String[] labelNames) {
        if (Objects.requireNonNull(type) == JOB_USAGE_BYTES_GAUGE) {
            return isEnabledViaConfig(JOB_USAGE_BYTES_GAUGE) ? new JobUsageBytesGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
        }
        return new NoOpMetricCollector<>();
    }

    public MetricCollector<FileStore, ? extends Collector> createFileStoreCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case FILE_STORE_AVAILABLE_GAUGE:
                return isEnabledViaConfig(FILE_STORE_AVAILABLE_GAUGE) ? new FileStoreAvailableGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case FILE_STORE_CAPACITY_GAUGE:
                return isEnabledViaConfig(FILE_STORE_CAPACITY_GAUGE) ? new FileStoreCapacityGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }
}
