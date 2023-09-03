package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Coverage;
import edu.hm.hafner.coverage.Metric;
import hudson.model.Run;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

import java.util.Optional;

public abstract class CoverageMetricsCollector<T, I extends SimpleCollector<?>> extends BuildsMetricCollector<T, I>  {
    protected CoverageMetricsCollector(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected Optional<Coverage> getCoverage(Run<?, ?> jenkinsRun, Metric metric, Baseline baseline) {

        CoverageBuildAction coverageBuildAction = jenkinsRun.getAction(CoverageBuildAction.class);
        if (coverageBuildAction == null) {
            return Optional.empty();
        }

        return coverageBuildAction.getAllValues(baseline).stream()
                .filter(value -> metric.equals(value.getMetric()) && value instanceof Coverage)
                .map(x -> (Coverage)x)
                .findFirst();
    }
}
