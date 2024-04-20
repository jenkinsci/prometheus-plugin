package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

public class BuildCollectorFactory extends BaseCollectorFactory {

    public MetricCollector<Run<?, ?>, ? extends Collector> createCollector(CollectorType type, String[] labelNames, String prefix) {
        switch (type) {
            case BUILD_DURATION_GAUGE:
                return saveBuildCollector(new BuildDurationGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_DURATION_SUMMARY:
                return saveBuildCollector(new BuildDurationSummary(labelNames, namespace, subsystem));
            case BUILD_FAILED_COUNTER:
                return saveBuildCollector(new BuildFailedCounter(labelNames, namespace, subsystem));
            case BUILD_RESULT_GAUGE:
                return saveBuildCollector(new BuildResultGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_RESULT_ORDINAL_GAUGE:
                return saveBuildCollector(new BuildResultOrdinalGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_START_GAUGE:
                return saveBuildCollector(new BuildStartGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_SUCCESSFUL_COUNTER:
                return saveBuildCollector(new BuildSuccessfulCounter(labelNames, namespace, subsystem));
            case FAILED_TESTS_GAUGE:
                return saveBuildCollector(new FailedTestsGauge(labelNames, namespace, subsystem, prefix));
            case SKIPPED_TESTS_GAUGE:
                return saveBuildCollector(new SkippedTestsGauge(labelNames, namespace, subsystem, prefix));
            case STAGE_SUMMARY:
                return saveBuildCollector(new StageSummary(labelNames, namespace, subsystem, prefix));
            case TOTAL_TESTS_GAUGE:
                return saveBuildCollector(new TotalTestsGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_LIKELY_STUCK_GAUGE:
                return saveBuildCollector(new BuildLikelyStuckGauge(labelNames, namespace, subsystem, prefix));
            case BUILD_ABORTED_COUNTER:
                return saveBuildCollector(new BuildAbortedCounter(labelNames, namespace, subsystem, prefix));
             case BUILD_UNSTABLE_COUNTER:
                return saveBuildCollector(new BuildUnstableCounter(labelNames, namespace, subsystem, prefix));
            case BUILD_TOTAL_COUNTER:
                return saveBuildCollector(new BuildTotalCounter(labelNames, namespace, subsystem, prefix));
            default:
                return new NoOpMetricCollector<>();
        }
    }
}
