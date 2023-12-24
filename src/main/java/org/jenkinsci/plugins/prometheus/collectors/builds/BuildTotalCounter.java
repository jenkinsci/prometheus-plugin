package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildTotalCounter extends BuildsMetricCollector<Run<?, ?>, Counter> {
    protected BuildTotalCounter(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected BuildTotalCounter(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_TOTAL_COUNTER;
    }

    @Override
    protected String getHelpText() {
        return "Total build count";
    }

    @Override
    protected SimpleCollector.Builder<?, Counter> getCollectorBuilder() {
        return Counter.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        // Increment counter every run that is completed.
        if(jenkinsObject.getResult() != Result.NOT_BUILT){
             this.collector.labels(labelValues).inc();
        }
    }
}
