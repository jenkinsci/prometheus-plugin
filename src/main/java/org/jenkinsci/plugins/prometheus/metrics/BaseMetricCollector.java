package org.jenkinsci.plugins.prometheus.metrics;

import hudson.model.Item;
import io.prometheus.client.Collector;

import java.util.List;

public abstract class BaseMetricCollector<T extends Item, I extends Collector> implements MetricCollector<T, I> {

    protected final static String SEPARATOR = "_";

    protected final String[] labelNames;
    protected final String namespace;
    protected final String subsystem;

    protected I collector;

    public BaseMetricCollector(String[] labelNames, String namespace, String subsystem) {
        this.labelNames = labelNames;
        this.namespace = namespace;
        this.subsystem = subsystem;
        collector = initCollector();
    }

    protected abstract I initCollector();

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        return collector.collect();
    }
}
