package org.jenkinsci.plugins.prometheus.metrics.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.metrics.BaseMetricCollector;

public class JobBuildResultOrdinalGauge extends BaseMetricCollector<Run, Gauge> {

    public JobBuildResultOrdinalGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
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
        if (jenkinsObject != null && jenkinsObject.getResult() != null ) {
            this.collector.labels(labelValues).set(jenkinsObject.getResult().ordinal);
        }
    }
}