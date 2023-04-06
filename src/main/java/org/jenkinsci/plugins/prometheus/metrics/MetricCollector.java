package org.jenkinsci.plugins.prometheus.metrics;

import hudson.model.Item;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;

import java.util.List;

/**
 * Implementations of this interface shall be able to construct and calculate any subclass of
 * {@link io.prometheus.client.SimpleCollector}
 * @param <T> - any subclass of {@link io.prometheus.client.SimpleCollector}
 */
public interface MetricCollector<T extends Item, I extends Collector> {

    /**
     * This method contains the logic to calculate a metric value based on the given Jenkins object (e.g. Job, Run,...)
     * @param jenkinsObject - Examples: {@link hudson.model.Job}, {@link hudson.model.Run}
     * @param labelValues - The label values for the calculation
     */
    void calculateMetric(T jenkinsObject, String[] labelValues);

    /**
     * Calling this method basically calls I.collect()
     */
    List<I.MetricFamilySamples> collect();
}
