package org.jenkinsci.plugins.prometheus.metrics;

import io.prometheus.client.Collector;

import java.util.List;

public abstract class BaseMetricCollector<T, I extends Collector> implements MetricCollector<T, I> {

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

    protected String calculateName(String name) {
        return getBaseName() + SEPARATOR + name;
    }

    private String getBaseName() {
        return "builds";
    }

}
