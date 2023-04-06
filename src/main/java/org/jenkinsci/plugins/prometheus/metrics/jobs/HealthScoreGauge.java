package org.jenkinsci.plugins.prometheus.metrics.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;

public class HealthScoreGauge extends BaseJobMetricCollector<Job, Gauge> {

    public HealthScoreGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("health_score"))
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(labelNames)
                .help("Health score of a job")
                .create();
    }

    @Override
    public void calculateMetric(Job jenkinsObject, String[] labelValues) {
        int score = jenkinsObject.getBuildHealth().getScore();
        this.collector.labels(labelValues).set(score);
    }

}
