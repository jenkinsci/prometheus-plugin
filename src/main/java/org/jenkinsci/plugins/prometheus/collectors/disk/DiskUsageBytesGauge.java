package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class DiskUsageBytesGauge extends BaseMetricCollector<DiskItem, Gauge> {

    protected DiskUsageBytesGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.DISK_USAGE_BYTES_GAUGE.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Disk usage of first level folder in JENKINS_HOME in bytes")
                .create();
    }

    @Override
    public void calculateMetric(DiskItem jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.labels(labelValues).set(jenkinsObject.getUsage() * 1024);
    }
}
