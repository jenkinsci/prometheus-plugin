package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class HealthScoreGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    protected HealthScoreGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.HEALTH_SCORE_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Health score of a job";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {
        int score = jenkinsObject.getBuildHealth().getScore();
        this.collector.labels(labelValues).set(score);
    }

}
