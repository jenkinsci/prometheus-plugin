package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.TestBasedMetricCollector;

public class FailedTestsGauge extends TestBasedMetricCollector<Run, Gauge> {

    protected FailedTestsGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName(CollectorType.BUILD_FAILED_TESTS.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Number of failing tests during the last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (!canBeCalculated(jenkinsObject)) {
            return;
        }

        int testsFailed = jenkinsObject.getAction(AbstractTestResultAction.class).getFailCount();
        collector.labels(labelValues).set(testsFailed);
    }
}
