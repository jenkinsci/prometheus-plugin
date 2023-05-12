package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class BuildCollectorFactory extends BaseCollectorFactory {

    public BuildCollectorFactory() {
        super();
    }

    public MetricCollector<Run, ? extends Collector> createCollector(CollectorType type, String[] labelNames, String prefix) {
        switch (type) {
            case BUILD_DURATION_GAUGE:
                return isEnabledViaConfig(BUILD_DURATION_GAUGE) ? new BuildDurationGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_DURATION_SUMMARY:
                return isEnabledViaConfig(BUILD_DURATION_SUMMARY) ? new BuildDurationSummary(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case BUILD_FAILED_COUNTER:
                return isEnabledViaConfig(BUILD_FAILED_COUNTER) ? new BuildFailedCounter(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case BUILD_RESULT_GAUGE:
                return isEnabledViaConfig(BUILD_RESULT_GAUGE) ? new BuildResultGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_RESULT_ORDINAL_GAUGE:
                return isEnabledViaConfig(BUILD_RESULT_ORDINAL_GAUGE) ? new BuildResultOrdinalGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_START_GAUGE:
                return isEnabledViaConfig(BUILD_START_GAUGE) ? new BuildStartGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_SUCCESSFUL_COUNTER:
                return isEnabledViaConfig(BUILD_SUCCESSFUL_COUNTER) ? new BuildSuccessfulCounter(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case FAILED_TESTS_GAUGE:
                return isEnabledViaConfig(FAILED_TESTS_GAUGE) ? new FailedTestsGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case SKIPPED_TESTS_GAUGE:
                return isEnabledViaConfig(SKIPPED_TESTS_GAUGE) ? new SkippedTestsGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case STAGE_SUMMARY:
                return isEnabledViaConfig(STAGE_SUMMARY) ? new StageSummary(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case TOTAL_TESTS_GAUGE:
                return isEnabledViaConfig(TOTAL_TESTS_GAUGE) ? new TotalTestsGauge(labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }
}
