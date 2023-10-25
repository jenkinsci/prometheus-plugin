package org.jenkinsci.plugins.prometheus;

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Node;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NodeCollector extends Collector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCollector.class);

    @Override
    public List<MetricFamilySamples> collect() {
        LOGGER.debug("Collecting node metrics for prometheus");
        String[] labelNameArray = {"computerName"};

        CollectorFactory factory = new CollectorFactory();

        MetricCollector<Executor, ? extends Collector> likelyStuckCollector = factory.createExecutorStatisticsCollector(CollectorType.EXECUTOR_LIKELY_STUCK_GAUGE, labelNameArray);

        List<? extends MetricCollector<Executor, ? extends Collector>> collectors = List.of(likelyStuckCollector);

        List<Computer> computers = Jenkins.get().getNodes().parallelStream()
                .map(Node::toComputer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Computer computer : computers) {
            String computerName = computer.getName();
            List<Executor> executors = computer.getExecutors();
            if (executors != null) {
                for (Executor ex : executors) {
                    likelyStuckCollector.calculateMetric(ex, new String[]{computerName});
                }
            }
        }

        return collectors.stream()
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
