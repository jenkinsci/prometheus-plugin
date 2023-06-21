package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.aggregators.MetricAggregator;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

public class BuildCollectorFactory extends BaseCollectorFactory {

    public BuildCollectorFactory() {
        super();
    }

    public MetricCollector<Run, ? extends Collector> createCollector(CollectorType type, MetricAggregator[] metricAggregators, String[] labelNames, String prefix) {
        switch (type) {
            case BUILD_DURATION_GAUGE:
                return isEnabledViaConfig(BUILD_DURATION_GAUGE) ? new BuildDurationGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_DURATION_SUMMARY:
                return isEnabledViaConfig(BUILD_DURATION_SUMMARY) ? new BuildDurationSummary(metricAggregators, labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case BUILD_FAILED_COUNTER:
                return isEnabledViaConfig(BUILD_FAILED_COUNTER) ? new BuildFailedCounter(metricAggregators, labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case BUILD_RESULT_GAUGE:
                return isEnabledViaConfig(BUILD_RESULT_GAUGE) ? new BuildResultGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_RESULT_ORDINAL_GAUGE:
                return isEnabledViaConfig(BUILD_RESULT_ORDINAL_GAUGE) ? new BuildResultOrdinalGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_START_GAUGE:
                return isEnabledViaConfig(BUILD_START_GAUGE) ? new BuildStartGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case BUILD_SUCCESSFUL_COUNTER:
                return isEnabledViaConfig(BUILD_SUCCESSFUL_COUNTER) ? new BuildSuccessfulCounter(metricAggregators, labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case FAILED_TESTS_GAUGE:
                return isEnabledViaConfig(FAILED_TESTS_GAUGE) ? new FailedTestsGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case SKIPPED_TESTS_GAUGE:
                return isEnabledViaConfig(SKIPPED_TESTS_GAUGE) ? new SkippedTestsGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case STAGE_SUMMARY:
                return isEnabledViaConfig(STAGE_SUMMARY) ? new StageSummary(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            case TOTAL_TESTS_GAUGE:
                return isEnabledViaConfig(TOTAL_TESTS_GAUGE) ? new TotalTestsGauge(metricAggregators, labelNames, namespace, subsystem, prefix) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }
}
