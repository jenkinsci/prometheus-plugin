package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;


public class NbBuildsGauge extends BuildsMetricCollector<Job, Gauge> {

    protected NbBuildsGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.NB_BUILDS_GAUGE.getName()))
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
