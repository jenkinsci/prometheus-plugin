package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Gauge;

public class BuildResultOrdinalGauge extends BuildsMetricCollector<Run, Gauge> {

    public BuildResultOrdinalGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("build_result_ordinal"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Build status of a job.")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
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
