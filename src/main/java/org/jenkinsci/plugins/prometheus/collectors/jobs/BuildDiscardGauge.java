package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import jenkins.model.BuildDiscarder;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class BuildDiscardGauge extends BuildsMetricCollector<Job, Gauge> {

    public BuildDiscardGauge(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("discard_active"))
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(labelNames)
                .help("Indicates if the build discarder is active for the given job")
                .create();
    }

    @Override
    public void calculateMetric(Job jenkinsObject, String[] labelValues) {
        BuildDiscarder buildDiscarder = jenkinsObject.getBuildDiscarder();
        double status = buildDiscarder != null ? 1.0 : 0.0;
        this.collector.labels(labelValues).set(status);
    }
}
