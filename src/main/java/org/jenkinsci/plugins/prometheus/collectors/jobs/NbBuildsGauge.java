package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;


public class NbBuildsGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    protected NbBuildsGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.NB_BUILDS_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Number of builds available for this job";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {
        int nbBuilds = jenkinsObject.getBuildsAsMap().size();
        this.collector.labels(labelValues).set(nbBuilds);
    }
}
