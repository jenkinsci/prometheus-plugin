package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class JobUsageBytesGauge extends BaseMetricCollector<JobDiskItem, Gauge> {

    protected JobUsageBytesGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.JOB_USAGE_BYTES_GAUGE.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Amount of disk usage (bytes) for each job in Jenkins")
                .create();
    }

    @Override
    public void calculateMetric(JobDiskItem jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.labels(labelValues).set(jenkinsObject.getUsage() * 1024);
    }
}
