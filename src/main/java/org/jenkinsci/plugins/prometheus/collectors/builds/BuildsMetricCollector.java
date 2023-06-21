package org.jenkinsci.plugins.prometheus.collectors.builds;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.aggregators.MetricAggregator;

import java.util.Arrays;
import java.util.List;

public abstract class BuildsMetricCollector <T, I extends Collector>  extends BaseMetricCollector<T, I> {

    private MetricAggregator[] metricAggregators;

    protected BuildsMetricCollector(MetricAggregator[] metricAggregators, String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
        this.metricAggregators = metricAggregators;
    }

    protected BuildsMetricCollector(MetricAggregator[] metricAggregators, String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
        this.metricAggregators = metricAggregators;
    }

    @Override
    public final void calculateMetric(T jenkinsObject, String[] labelValues) {
        boolean shouldAddDefaultLabel = true;
        for (MetricAggregator aggregator : metricAggregators) {
            if (aggregator.isHandleJenkinsObject(jenkinsObject)) {
                String[] aggregatedLabelValues = Arrays.copyOf(labelValues, labelValues.length);
                aggregatedLabelValues = aggregator.getLabelValues(jenkinsObject, aggregatedLabelValues);
                calculateBuildMetric(jenkinsObject, aggregatedLabelValues);
                shouldAddDefaultLabel = false;
            }
        }
        if (shouldAddDefaultLabel) {
            calculateBuildMetric(jenkinsObject, labelValues);
        }
    }
    protected abstract void calculateBuildMetric(T jenkinsObject, String[] labelValues);

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        return collector.collect();
    }

    @Override
    protected String getBaseName() {
        return "builds";
    }
}
