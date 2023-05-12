package org.jenkinsci.plugins.prometheus;

import hudson.model.Label;
import hudson.model.LoadStatistics;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutorCollector extends Collector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorCollector.class);
    @Override
    public List<MetricFamilySamples> collect() {
        LOGGER.debug("Collecting executor metrics for prometheus");

        CollectorFactory factory = new CollectorFactory();

        String prefix = "executors";
        String[] labelNameArray = {"label"};

        List<MetricCollector<LoadStatistics.LoadStatisticsSnapshot, ? extends Collector>> collectors = new ArrayList<>();

        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_AVAILABLE_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_BUSY_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_CONNECTING_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_DEFINED_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_IDLE_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_ONLINE_GAUGE, labelNameArray, prefix));
        collectors.add(factory.createExecutorCollector(CollectorType.EXECUTORS_QUEUE_LENGTH_GAUGE, labelNameArray, prefix));

        LOGGER.debug("getting load statistics for Executors");

        Label[] labels = Jenkins.get().getLabels().toArray(new Label[0]);
        for (Label l : labels) {
            collectors.forEach(c -> c.calculateMetric(l.loadStatistics.computeSnapshot(), new String[] {l.getDisplayName()}));
        }

        return collectors.stream()
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
