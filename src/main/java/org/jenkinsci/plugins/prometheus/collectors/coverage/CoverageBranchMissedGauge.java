package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Coverage;
import edu.hm.hafner.coverage.Metric;
import hudson.model.Run;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

import java.util.Optional;

public class CoverageBranchMissedGauge extends CoverageMetricsCollector<Run<?, ?>, Gauge> {

    protected CoverageBranchMissedGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.COVERAGE_BRANCH_MISSED;
    }

    @Override
    protected String getHelpText() {
        return "Returns the number of branches missed";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {

        Optional<Coverage> optional = getCoverage(jenkinsObject, Metric.BRANCH, Baseline.PROJECT);
        if (optional.isEmpty()) {
            collector.labels(labelValues).set(-1);
            return;
        }

        Coverage coverage = optional.get();
        collector.labels(labelValues).set(coverage.getMissed());
    }
}
