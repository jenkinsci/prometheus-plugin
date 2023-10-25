package org.jenkinsci.plugins.prometheus.collectors.nodes;

import hudson.model.Executor;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class ExecutorLikelyStuckGauge extends BaseMetricCollector<Executor, Gauge> {

    protected ExecutorLikelyStuckGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.EXECUTOR_LIKELY_STUCK_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Returns an indication if an executor of a node is likely stuck";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Executor executor, String[] labelValues) {
        if (executor == null) {
            return;
        }
        boolean likelyStuck = executor.isLikelyStuck();
        collector.labels(labelValues).set(likelyStuck ? 1.0 : 0.0);
    }

}
