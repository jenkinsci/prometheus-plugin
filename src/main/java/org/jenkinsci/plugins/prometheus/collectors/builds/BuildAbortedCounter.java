package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildAbortedCounter extends BuildsMetricCollector<Run<?, ?>, Counter>  {
    protected BuildAbortedCounter(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected BuildAbortedCounter(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_ABORTED_COUNTER;
    }

    @Override
    protected String getHelpText() {
        return "aborted build count";
    }

    @Override
    protected SimpleCollector.Builder<?, Counter> getCollectorBuilder() {
        return Counter.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        // Increment counter if result was unstable.
        if(jenkinsObject.getResult() == Result.ABORTED){
            this.collector.labels(labelValues).inc();
        }
    }
}
