package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.jenkins.JenkinsUpGauge;
import org.jenkinsci.plugins.prometheus.collectors.jenkins.JenkinsUptimeGauge;
import org.jenkinsci.plugins.prometheus.collectors.jenkins.JenkinsVersionInfo;
import org.jenkinsci.plugins.prometheus.collectors.jenkins.NodesOnlineGauge;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

public class JenkinsStatusCollector extends Collector {
    protected String subsystem;
    protected String namespace;
    protected Jenkins jenkins;

    @Override
    public List<MetricFamilySamples> collect() {
        subsystem = ConfigurationUtils.getSubSystem();
        namespace = ConfigurationUtils.getNamespace();

        JenkinsUptimeGauge jenkinsUptime = new JenkinsUptimeGauge(new String[]{}, namespace, subsystem);
        JenkinsUpGauge jenkinsUp = new JenkinsUpGauge(new String[]{}, namespace, subsystem);
        JenkinsVersionInfo versionInfo = new JenkinsVersionInfo(new String[]{}, namespace, subsystem);

        jenkins = Jenkins.get();

        jenkinsUptime.calculateMetric(jenkins, new String[]{});
        jenkinsUp.calculateMetric(jenkins, new String[]{});
        versionInfo.calculateMetric(jenkins, new String[]{});

        List<MetricFamilySamples> samples = new ArrayList<>();
        samples.addAll(jenkinsUptime.collect());
        samples.addAll(jenkinsUp.collect());
        samples.addAll(versionInfo.collect());

        if (!PrometheusConfiguration.get().isCollectNodeStatus()) {
            return samples;
        }

        samples.addAll(collectNodeStatus());

        return samples;
    }

    protected List<MetricFamilySamples> collectNodeStatus() {
        NodesOnlineGauge nodesOnlineGauge = new NodesOnlineGauge(new String[]{}, namespace, subsystem);
        nodesOnlineGauge.calculateMetric(jenkins, new String[]{});
        return nodesOnlineGauge.collect();
    }
}
