package org.jenkinsci.plugins.prometheus.collectors.executors;

import hudson.model.LoadStatistics;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class ExecutorCollectorFactory extends BaseCollectorFactory {

    public ExecutorCollectorFactory() {
        super();
    }

    public MetricCollector<LoadStatistics.LoadStatisticsSnapshot, ? extends Collector> createCollector(CollectorType type, String[] labelNames, String prefix) {
        switch (type) {
            case EXECUTORS_AVAILABLE_GAUGE:
                return saveBuildCollector(new ExecutorsAvailableGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_BUSY_GAUGE:
                return saveBuildCollector(new ExecutorsBusyGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_CONNECTING_GAUGE:
                return saveBuildCollector(new ExecutorsConnectingGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_DEFINED_GAUGE:
                return saveBuildCollector(new ExecutorsDefinedGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_IDLE_GAUGE:
                return saveBuildCollector(new ExecutorsIdleGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_ONLINE_GAUGE:
                return saveBuildCollector(new ExecutorsOnlineGauge(labelNames, namespace, subsystem, prefix));
            case EXECUTORS_QUEUE_LENGTH_GAUGE:
                return saveBuildCollector(new ExecutorsQueueLengthGauge(labelNames, namespace, subsystem, prefix));
            default:
                return new NoOpMetricCollector<>();
        }
    }

}
