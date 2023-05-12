package org.jenkinsci.plugins.prometheus.collectors;

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

public class NoOpMetricCollector<T, I extends Collector> implements MetricCollector<T, I> {
    @Override
    public void calculateMetric(Object jenkinsObject, String[] labelValues) {
        // do nothing
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        // do nothing
        return new ArrayList<>();
    }
}
