package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.aggregators.MetricAggregator;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class HealthScoreGauge extends BuildsMetricCollector<Job, Gauge> {

    protected HealthScoreGauge(MetricAggregator[] metricAggregators, String[] labelNames, String namespace, String subSystem) {
        super(metricAggregators, labelNames, namespace, subSystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.HEALTH_SCORE_GAUGE.getName()))
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(labelNames)
                .help("Health score of a job")
                .create();
    }

    @Override
    public void calculateBuildMetric(Job jenkinsObject, String[] labelValues) {
        int score = jenkinsObject.getBuildHealth().getScore();
        this.collector.labels(labelValues).set(score);
    }

}
