package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class JenkinsUpGauge extends BaseMetricCollector<Jenkins, Gauge> {

    JenkinsUpGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(CollectorType.JENKINS_UP_GAUGE.getName())
                .labelNames()
                .subsystem(subsystem)
                .namespace(namespace)
                .help("Is Jenkins ready to receive requests")
                .create();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.set(jenkinsObject.getInitLevel() == hudson.init.InitMilestone.COMPLETED ? 1 : 0);
    }
}
