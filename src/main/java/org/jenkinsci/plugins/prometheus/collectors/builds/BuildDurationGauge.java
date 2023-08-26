package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildDurationGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    protected BuildDurationGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_DURATION_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Build times in milliseconds of last build";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (!jenkinsObject.isBuilding()) {
            collector.labels(labelValues).set(jenkinsObject.getDuration());
        }
    }
}
