package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

/**
 * All Collectors need to be created via the CollectorFactory
 */
public class JenkinsCollectorFactory extends BaseCollectorFactory {

    public JenkinsCollectorFactory() {
        super();
    }

    public MetricCollector<Jenkins, ? extends Collector> createCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case JENKINS_UP_GAUGE:
                return saveBuildCollector(new JenkinsUpGauge(labelNames, namespace, subsystem));
            case NODES_ONLINE_GAUGE:
                if (!isNodeOnlineGaugeEnabled()) {
                    return new NoOpMetricCollector<>();
                }
                return saveBuildCollector(new NodesOnlineGauge(labelNames, namespace, subsystem));
            case JENKINS_UPTIME_GAUGE:
                return saveBuildCollector(new JenkinsUptimeGauge(labelNames, namespace, subsystem));
            case JENKINS_VERSION_INFO_GAUGE:
                return saveBuildCollector(new JenkinsVersionInfo(labelNames, namespace, subsystem));
            default:
                return new NoOpMetricCollector<>();
        }
    }

    private boolean isNodeOnlineGaugeEnabled() {
        return PrometheusConfiguration.get().isCollectNodeStatus();
    }

}
