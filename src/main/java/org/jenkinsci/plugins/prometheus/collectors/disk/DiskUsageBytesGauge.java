package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class DiskUsageBytesGauge extends BaseMetricCollector<DiskItem, Gauge> {

    public DiskUsageBytesGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("disk_usage_bytes"))
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
