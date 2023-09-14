package org.jenkinsci.plugins.prometheus.service;

import hudson.ExtensionList;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import jenkins.metrics.api.Metrics;
import org.jenkinsci.plugins.prometheus.*;
import org.jenkinsci.plugins.prometheus.config.disabledmetrics.MetricStatusChecker;
import org.jenkinsci.plugins.prometheus.util.JenkinsNodeBuildsSampleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultPrometheusMetrics implements PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);

    private final CollectorRegistry collectorRegistry;
    private final AtomicReference<String> cachedMetrics;

    public DefaultPrometheusMetrics() {
        CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        collectorRegistry.register(new JobCollector());
        collectorRegistry.register(new JenkinsStatusCollector());
        collectorRegistry.register(new DropwizardExports(Metrics.metricRegistry(), new JenkinsNodeBuildsSampleBuilder()));
        collectorRegistry.register(new DiskUsageCollector());
        collectorRegistry.register(new ExecutorCollector());
        collectorRegistry.register(new CodeCoverageCollector());

        // other collectors from other plugins
        ExtensionList.lookup(Collector.class).forEach(collectorRegistry::register);

        DefaultExports.initialize();

        this.collectorRegistry = collectorRegistry;
        this.cachedMetrics = new AtomicReference<>("");
    }

    @Override
    public String getMetrics() {
        return cachedMetrics.get();
    }

    @Override
    public void collectMetrics() {
        try (StringWriter buffer = new StringWriter()) {
            Set<String> filteredMetrics = MetricStatusChecker.filter(getMetricNames());
            TextFormat.write004(buffer, collectorRegistry.filteredMetricFamilySamples(filteredMetrics));
            cachedMetrics.set(buffer.toString());
        } catch (IOException e) {
            logger.debug("Unable to collect metrics");
        }
    }

    private List<String> getMetricNames() {
        Enumeration<Collector.MetricFamilySamples> metricFamilySamplesEnumeration = collectorRegistry.metricFamilySamples();
        List<String> allMetricNames = new ArrayList<>();
        while (metricFamilySamplesEnumeration.hasMoreElements()) {
            Collector.MetricFamilySamples familySamples = metricFamilySamplesEnumeration.nextElement();
            if (familySamples != null && familySamples.name != null) {
                allMetricNames.add(familySamples.name);
            }
        }
        return allMetricNames;
    }
}
