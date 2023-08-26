package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import jenkins.model.BuildDiscarder;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class BuildDiscardGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    protected BuildDiscardGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_DISCARD_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Indicates if the build discarder is active for the given job";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {
        BuildDiscarder buildDiscarder = jenkinsObject.getBuildDiscarder();
        double status = buildDiscarder != null ? 1.0 : 0.0;
        this.collector.labels(labelValues).set(status);
    }
}
