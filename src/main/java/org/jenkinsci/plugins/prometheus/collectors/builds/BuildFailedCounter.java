package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildFailedCounter extends BuildsMetricCollector<Run<?, ?>, Counter>  {
    protected BuildFailedCounter(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected BuildFailedCounter(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_FAILED_COUNTER;
    }

    @Override
    protected String getHelpText() {
        return "Failed build count";
    }

    @Override
    protected SimpleCollector.Builder<?, Counter> getCollectorBuilder() {
        return Counter.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        // increment counter if the build failed.
        if(jenkinsObject.getResult() == Result.FAILURE){
            this.collector.labels(labelValues).inc();
        }
    }
}
