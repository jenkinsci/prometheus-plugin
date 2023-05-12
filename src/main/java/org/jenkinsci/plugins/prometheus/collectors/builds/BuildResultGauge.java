package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Gauge;

public class BuildResultGauge extends BuildsMetricCollector<Run, Gauge> {

    public BuildResultGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("build_result"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Build status of a job as a boolean (0 or 1)")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
            /*
             * _last_build_result _last_build_result_ordinal
             *
             * SUCCESS   0 true  - The build had no errors.
             * UNSTABLE  1 true  - The build had some errors but they were not fatal. For example, some tests failed.
             * FAILURE   2 false - The build had a fatal error.
             * NOT_BUILT 3 false - The module was not built.
             * ABORTED   4 false - The build was manually aborted.
             */
            int ordinal = -1;
            var runResult = jenkinsObject.getResult();
            if (null != runResult) {
                ordinal = runResult.ordinal;
            }
            collector.labels(labelValues).set(ordinal < 2 ? 1 : 0);
    }
}
