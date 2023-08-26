package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import hudson.model.Computer;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

import java.time.Clock;

public class JenkinsUptimeGauge extends BaseMetricCollector<Jenkins, Gauge> {

    JenkinsUptimeGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.JENKINS_UPTIME_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Time since Jenkins machine was initialized";
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
        Computer computer = jenkinsObject.toComputer();
        if (computer == null) {
            return;
        }
        long upTime = computer.getConnectTime();
        // Using Clock to be able to mock in test
        collector.set(Clock.systemUTC().millis() - upTime);
    }
}
