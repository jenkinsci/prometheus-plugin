package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Info;
import io.prometheus.client.SimpleCollector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class JenkinsVersionInfo extends BaseMetricCollector<Jenkins, Info> {

    JenkinsVersionInfo(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.JENKINS_VERSION_INFO_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Jenkins Application Version";
    }

    @Override
    protected SimpleCollector.Builder<?, Info> getCollectorBuilder() {
        return Info.build();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.info("version", Jenkins.VERSION);
    }
}
