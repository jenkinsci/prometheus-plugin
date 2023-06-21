package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.aggregators.MetricAggregator;

public class BuildStartGauge extends BuildsMetricCollector<Run, Gauge> {

    protected BuildStartGauge(MetricAggregator[] metricAggregators, String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(metricAggregators, labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.BUILD_START_GAUGE.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Last build start timestamp in milliseconds")
                .create();
    }

    @Override
    public void calculateBuildMetric(Run jenkinsObject, String[] labelValues) {
        long millis = jenkinsObject.getStartTimeInMillis();
        collector.labels(labelValues).set(millis);
    }
}
