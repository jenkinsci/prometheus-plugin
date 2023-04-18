package org.jenkinsci.plugins.prometheus.metrics.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;


public class NbBuildsGauge extends BaseJobMetricCollector<Job, Gauge> {

    public NbBuildsGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("available_builds_count"))
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(labelNames)
                .help("Number of builds available for this job")
                .create();
    }

    @Override
    public void calculateMetric(Job jenkinsObject, String[] labelValues) {
        int nbBuilds = jenkinsObject.getBuildsAsMap().size();
        this.collector.labels(labelValues).set(nbBuilds);
    }
}
