package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class JenkinsQuietDownGauge extends BaseMetricCollector<Jenkins, Gauge> {

    JenkinsQuietDownGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.JENKINS_QUIETDOWN_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Is Jenkins in quiet mode";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.set(jenkinsObject.isQuietingDown() ? 1 : 0);
    }
}
