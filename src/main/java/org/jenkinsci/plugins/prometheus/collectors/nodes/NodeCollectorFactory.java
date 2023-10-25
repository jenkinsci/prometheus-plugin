package org.jenkinsci.plugins.prometheus.collectors.nodes;

import hudson.model.Executor;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

import java.util.stream.Collector;

public class NodeCollectorFactory extends BaseCollectorFactory {

    public NodeCollectorFactory(){super();}

    public MetricCollector<Executor, ? extends Collector> createExecutorCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case EXECUTOR_LIKELY_STUCK_GAUGE:
                return saveBuildCollector(new ExecutorLikelyStuckGauge(labelNames, namespace, subsystem));
            default:
                return new NoOpMetricCollector<>();
        }
    }
}
