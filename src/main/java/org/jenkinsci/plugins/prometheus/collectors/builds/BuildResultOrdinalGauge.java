package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildResultOrdinalGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    protected BuildResultOrdinalGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_RESULT_ORDINAL_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Build status of a job.";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (this.collector == null) {
            return;
        }

        if (jenkinsObject == null) {
            return;
        }

        Result result = jenkinsObject.getResult();
        if (result == null) {
            return;
        }

        if (labelValues == null) {
            this.collector.labels().set(result.ordinal);
        } else {
            this.collector.labels(labelValues).set(result.ordinal);
        }


    }
}
