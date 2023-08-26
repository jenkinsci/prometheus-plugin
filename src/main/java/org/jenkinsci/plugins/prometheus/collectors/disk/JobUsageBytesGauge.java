package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class JobUsageBytesGauge extends BaseMetricCollector<JobDiskItem, Gauge> {

    protected JobUsageBytesGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.JOB_USAGE_BYTES_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Amount of disk usage (bytes) for each job in Jenkins";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(JobDiskItem jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.labels(labelValues).set(jenkinsObject.getUsage() * 1024);
    }
}
