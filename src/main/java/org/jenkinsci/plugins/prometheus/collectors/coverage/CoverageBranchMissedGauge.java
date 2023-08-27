package org.jenkinsci.plugins.prometheus.collectors.coverage;

import hudson.model.Run;
import io.jenkins.plugins.coverage.model.Coverage;
import io.jenkins.plugins.coverage.model.CoverageBuildAction;
import io.jenkins.plugins.coverage.model.CoverageMetric;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class CoverageBranchMissedGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

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

        CoverageBuildAction coverageBuildAction = jenkinsObject.getAction(CoverageBuildAction.class);
        if (coverageBuildAction == null) {
            return;
        }
        Coverage classCoverage = coverageBuildAction.getCoverage(CoverageMetric.BRANCH);
        if (classCoverage == null) {
            return;
        }
        collector.labels(labelValues).set(classCoverage.getMissed());
    }
}
