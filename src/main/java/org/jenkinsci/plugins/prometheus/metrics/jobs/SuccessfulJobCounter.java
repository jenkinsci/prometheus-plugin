package org.jenkinsci.plugins.prometheus.metrics.jobs;

import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Counter;

public class SuccessfulJobCounter extends BaseJobMetricCollector<Run, Counter> {

    public SuccessfulJobCounter(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected Counter initCollector() {
        return Counter.build()
                .name(calculateName("success_build_count"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Successful build count")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        Result runResult = jenkinsObject.getResult();
        if (runResult != null && !jenkinsObject.isBuilding()) {
            if (runResult.equals(Result.SUCCESS) || runResult.equals(Result.UNSTABLE)) {
                this.collector.labels(labelValues).inc();
            }
        }
    }
}
