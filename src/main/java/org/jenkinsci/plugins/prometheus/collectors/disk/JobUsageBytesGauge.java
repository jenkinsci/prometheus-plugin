package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class JobUsageBytesGauge extends BaseMetricCollector<JobDiskItem, Gauge> {

    public JobUsageBytesGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("job_usage_bytes"))
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
