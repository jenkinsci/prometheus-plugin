package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.TestBasedMetricCollector;

public class SkippedTestsGauge extends TestBasedMetricCollector<Run<?, ?>, Gauge> {

    protected SkippedTestsGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.SKIPPED_TESTS_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Number of skipped tests during the last build";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (!canBeCalculated(jenkinsObject)) {
            return;
        }

        int testsSkipped = jenkinsObject.getAction(AbstractTestResultAction.class).getSkipCount();
        collector.labels(labelValues).set(testsSkipped);
    }
}
