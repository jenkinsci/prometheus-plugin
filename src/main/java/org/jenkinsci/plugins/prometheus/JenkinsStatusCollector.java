package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsStatusCollector extends Collector {
    @Override
    public List<MetricFamilySamples> collect() {

        CollectorFactory factory = new CollectorFactory();

        List<MetricCollector<Jenkins, ? extends Collector>> collectors = new ArrayList<>();

        collectors.add(factory.createJenkinsCollector(CollectorType.JENKINS_VERSION_INFO_GAUGE, new String[]{}));
        collectors.add(factory.createJenkinsCollector(CollectorType.JENKINS_UP_GAUGE, new String[]{}));
        collectors.add(factory.createJenkinsCollector(CollectorType.JENKINS_UPTIME_GAUGE, new String[]{}));
        collectors.add(factory.createJenkinsCollector(CollectorType.NODES_ONLINE_GAUGE, new String[]{}));

        collectors.forEach(c -> c.calculateMetric(Jenkins.get(), new String[]{}));

        return collectors.stream()
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }
}
