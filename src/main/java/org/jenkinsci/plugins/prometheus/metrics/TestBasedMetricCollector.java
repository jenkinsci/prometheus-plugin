package org.jenkinsci.plugins.prometheus.metrics;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

public abstract class TestBasedMetricCollector<T, I extends Collector> extends BaseMetricCollector<T, I> {

    public TestBasedMetricCollector(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    public TestBasedMetricCollector(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }


    protected boolean canBeCalculated(Run run) {
        if (run == null) {
            return false;
        }

        if (run.isBuilding()) {
            return false;
        }

        return PrometheusConfiguration.get().isFetchTestResults() && hasTestResults(run);
    }


    private boolean hasTestResults(Run<?, ?> job) {
        return job.getAction(AbstractTestResultAction.class) != null;
    }
}
