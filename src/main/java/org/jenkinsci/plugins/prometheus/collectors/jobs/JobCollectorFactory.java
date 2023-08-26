package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

public class JobCollectorFactory extends BaseCollectorFactory {

    public JobCollectorFactory() {
        super();
    }

    public MetricCollector<Job<?, ?>, ? extends Collector> createCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case HEALTH_SCORE_GAUGE:
                return saveBuildCollector(new HealthScoreGauge(labelNames, namespace, subsystem));
            case NB_BUILDS_GAUGE:
                return saveBuildCollector(new NbBuildsGauge(labelNames, namespace, subsystem));
            case BUILD_DISCARD_GAUGE:
                return saveBuildCollector(new BuildDiscardGauge(labelNames, namespace, subsystem));
            case CURRENT_RUN_DURATION_GAUGE:
                return saveBuildCollector(new CurrentRunDurationGauge(labelNames, namespace, subsystem));
            default:
                return new NoOpMetricCollector<>();
        }
    }

}
