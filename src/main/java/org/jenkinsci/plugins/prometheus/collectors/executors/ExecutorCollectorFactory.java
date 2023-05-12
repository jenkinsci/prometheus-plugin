package org.jenkinsci.plugins.prometheus.collectors.executors;

import hudson.model.LoadStatistics;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class ExecutorCollectorFactory extends BaseCollectorFactory {

    public ExecutorCollectorFactory() {
        super();
    }

    public MetricCollector<LoadStatistics.LoadStatisticsSnapshot, ? extends Collector> createCollector(CollectorType type, String[] labelNames, String prefix) {
        switch (type) {
            case EXECUTORS_AVAILABLE_GAUGE:
                return isEnabledViaConfig(EXECUTORS_AVAILABLE_GAUGE) ? new ExecutorsAvailableGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_BUSY_GAUGE:
                return isEnabledViaConfig(EXECUTORS_BUSY_GAUGE) ? new ExecutorsBusyGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_CONNECTING_GAUGE:
                return isEnabledViaConfig(EXECUTORS_CONNECTING_GAUGE) ? new ExecutorsConnectingGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_DEFINED_GAUGE:
                return isEnabledViaConfig(EXECUTORS_DEFINED_GAUGE) ? new ExecutorsDefinedGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_IDLE_GAUGE:
                return isEnabledViaConfig(EXECUTORS_IDLE_GAUGE) ? new ExecutorsIdleGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_ONLINE_GAUGE:
                return isEnabledViaConfig(EXECUTORS_ONLINE_GAUGE) ? new ExecutorsOnlineGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case EXECUTORS_QUEUE_LENGTH_GAUGE:
                return isEnabledViaConfig(EXECUTORS_QUEUE_LENGTH_GAUGE) ? new ExecutorsQueueLengthGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }

}
