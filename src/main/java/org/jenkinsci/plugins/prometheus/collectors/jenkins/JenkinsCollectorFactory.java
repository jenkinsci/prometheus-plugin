package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import static org.jenkinsci.plugins.prometheus.collectors.CollectorType.*;

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
                return isEnabledViaConfig(JENKINS_UP_GAUGE) ? new JenkinsUpGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case NODES_ONLINE_GAUGE:
                return isEnabledViaConfig(NODES_ONLINE_GAUGE) && isNodeOnlineGaugeEnabled() ? new NodesOnlineGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case JENKINS_UPTIME_GAUGE:
                return isEnabledViaConfig(JENKINS_UPTIME_GAUGE) ? new JenkinsUptimeGauge(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            case JENKINS_VERSION_INFO_GAUGE:
                return isEnabledViaConfig(JENKINS_VERSION_INFO_GAUGE) ? new JenkinsVersionInfo(labelNames, namespace, subsystem) : new NoOpMetricCollector<>();
            default:
                return new NoOpMetricCollector<>();
        }
    }

    private boolean isNodeOnlineGaugeEnabled() {
        return PrometheusConfiguration.get().isCollectNodeStatus();
    }

}
