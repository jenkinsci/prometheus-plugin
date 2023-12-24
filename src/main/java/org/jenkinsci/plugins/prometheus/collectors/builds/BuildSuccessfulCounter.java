package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildSuccessfulCounter extends BuildsMetricCollector<Run<?, ?>, Counter> {
    protected BuildSuccessfulCounter(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected BuildSuccessfulCounter(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_SUCCESSFUL_COUNTER;
    }

    @Override
    protected String getHelpText() {
        return "Successful build count";
    }

    @Override
    protected SimpleCollector.Builder<?, Counter> getCollectorBuilder() {
        return Counter.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        // Increment the counter if the result of run was successful.
        if(jenkinsObject.getResult() == Result.SUCCESS){
            this.collector.labels(labelValues).inc();
        } 
    }
}
