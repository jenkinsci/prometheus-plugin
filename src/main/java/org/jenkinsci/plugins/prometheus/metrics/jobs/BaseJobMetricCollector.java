package org.jenkinsci.plugins.prometheus.metrics.jobs;


import hudson.model.Item;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.metrics.BaseMetricCollector;

abstract class BaseJobMetricCollector<T extends Item, I extends Collector> extends BaseMetricCollector<T, I> {

    public BaseJobMetricCollector(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    protected String calculateName(String name) {
        return getBaseName() + SEPARATOR + name;
    }

    private String getBaseName() {
        return "builds";
    }
}
