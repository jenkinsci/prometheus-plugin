package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.TestBasedMetricCollector;

public class TotalTestsGauge extends TestBasedMetricCollector<Run, Gauge> {
    protected TotalTestsGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return  Gauge.build()
                .name(calculateName(CollectorType.TOTAL_TESTS_GAUGE.getName()))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Number of total tests during the last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (!canBeCalculated(jenkinsObject)) {
            return;
        }

        int testsTotal = jenkinsObject.getAction(AbstractTestResultAction.class).getTotalCount();
        this.collector.labels(labelValues).set(testsTotal);
    }
}
