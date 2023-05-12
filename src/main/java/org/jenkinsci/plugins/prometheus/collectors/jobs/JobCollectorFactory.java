package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class JobCollectorFactory extends BaseCollectorFactory {

    public JobCollectorFactory() {
        super();
    }

    public MetricCollector<Job, ? extends Collector> createCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case HEALTH_SCORE_GAUGE:
                return isEnabledViaConfig(HEALTH_SCORE_GAUGE) ? new HealthScoreGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case NB_BUILDS_GAUGE:
                return isEnabledViaConfig(NB_BUILDS_GAUGE) ? new NbBuildsGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case BUILD_DISCARD_GAUGE:
                return isEnabledViaConfig(BUILD_DISCARD_GAUGE) ? new BuildDiscardGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case CURRENT_RUN_DURATION_GAUGE:
                return isEnabledViaConfig(CURRENT_RUN_DURATION_GAUGE) ? new CurrentRunDurationGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }

}
