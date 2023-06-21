package org.jenkinsci.plugins.prometheus.collectors.aggregators;

public interface MetricAggregator {
    boolean isHandleJenkinsObject(Object jenkinsObject);
    String[] getLabelValues(Object jenkinsObject, String[] aggregatedLabelValues);
}
