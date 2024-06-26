package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildStartGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    protected BuildStartGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_START_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Last build start timestamp in milliseconds";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        long millis = jenkinsObject.getStartTimeInMillis();
        collector.labels(labelValues).set(millis);
    }
}
