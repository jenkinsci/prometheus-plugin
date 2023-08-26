package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

import java.time.Clock;

public class CurrentRunDurationGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    protected CurrentRunDurationGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.CURRENT_RUN_DURATION_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Indicates the runtime of the run currently building if there is a run currently building";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job jenkinsObject, String[] labelValues) {

        Run runningBuild = jenkinsObject.getLastBuild();
        if (runningBuild != null && runningBuild.isBuilding()) {
            long start = runningBuild.getStartTimeInMillis();
            // Using Clock to be able to mock in test
            long end = Clock.systemUTC().millis();
            long duration = Math.max(end - start, 0);
            this.collector.labels(labelValues).set(duration);
        }
    }
}
