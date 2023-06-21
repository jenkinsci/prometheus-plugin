package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.aggregators.MetricAggregator;

public class BuildDurationGauge extends BuildsMetricCollector<Run, Gauge> {

    protected BuildDurationGauge(MetricAggregator[] metricAggregators, String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(metricAggregators, labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.BUILD_DURATION_GAUGE.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Build times in milliseconds of last build")
                .create();
    }

    @Override
    public void calculateBuildMetric(Run jenkinsObject, String[] labelValues) {
        if (!jenkinsObject.isBuilding()) {
            collector.labels(labelValues).set(jenkinsObject.getDuration());
        }
    }
}
