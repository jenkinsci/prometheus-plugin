package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class BuildStartGauge extends BuildsMetricCollector<Run, Gauge> {

    public BuildStartGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("build_start_time_milliseconds"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Last build start timestamp in milliseconds")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        long millis = jenkinsObject.getStartTimeInMillis();
        collector.labels(labelValues).set(millis);
    }
}
