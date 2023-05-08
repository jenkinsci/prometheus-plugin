package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class BuildDurationGauge extends BuildsMetricCollector<Run, Gauge> {

    public BuildDurationGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("build_duration_milliseconds"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Build times in milliseconds of last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (!jenkinsObject.isBuilding()) {
            collector.labels(labelValues).set(jenkinsObject.getDuration());
        }
    }
}
