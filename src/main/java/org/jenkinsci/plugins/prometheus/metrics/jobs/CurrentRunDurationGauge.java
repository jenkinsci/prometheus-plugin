package org.jenkinsci.plugins.prometheus.metrics.jobs;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.util.ArrayUtils;

import java.time.Clock;

public class CurrentRunDurationGauge extends BaseJobMetricCollector<Job, Gauge> {

    public CurrentRunDurationGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("running_build_duration_milliseconds"))
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(ArrayUtils.appendToArray(labelNames, "building"))
                .help("Indicates the runtime of the run currently building if there is a run currently building")
                .create();
    }

    @Override
    public void calculateMetric(Job jenkinsObject, String[] labelValues) {
        boolean isBuilding = jenkinsObject.isBuilding();

        String[] labels = ArrayUtils.appendToArray(labelValues, String.valueOf(isBuilding));
        if (isBuilding) {
            Run runningBuild = jenkinsObject.getLastBuild();
            if (runningBuild != null) {
                long start = runningBuild.getStartTimeInMillis();
                // Using Clock to be able to mock in test
                long end = Clock.systemUTC().millis();
                long duration = Math.max(end - start, 0);
                this.collector.labels(labels).set(duration);
                return;
            }
        }

        this.collector.labels(labels).set(0.0);
    }
}
