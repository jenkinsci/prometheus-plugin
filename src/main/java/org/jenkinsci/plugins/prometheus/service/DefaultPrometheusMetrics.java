package org.jenkinsci.plugins.prometheus.service;

import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;
import jenkins.metrics.api.Metrics;
import org.jenkinsci.plugins.prometheus.CodeCoverageCollector;
import org.jenkinsci.plugins.prometheus.DiskUsageCollector;
import org.jenkinsci.plugins.prometheus.ExecutorCollector;
import org.jenkinsci.plugins.prometheus.JenkinsStatusCollector;
import org.jenkinsci.plugins.prometheus.JobCollector;
import org.jenkinsci.plugins.prometheus.config.disabledmetrics.FilteredMetricEnumeration;
import org.jenkinsci.plugins.prometheus.util.JenkinsNodeBuildsSampleBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Restricted(NoExternalUse.class)
public class DefaultPrometheusMetrics implements PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);

    private volatile boolean initialized = false;
    private volatile boolean initializing = false;
    private final CollectorRegistry collectorRegistry;
    private final AtomicReference<String> cachedMetrics;

    public DefaultPrometheusMetrics() {
        this.collectorRegistry = CollectorRegistry.defaultRegistry;
        this.cachedMetrics = new AtomicReference<>("");
    }

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void init() {
        if (!initialized && !initializing) {
            initializing = true;
            logger.debug("Initializing...");
            collectorRegistry.register(new JobCollector());
            collectorRegistry.register(new JenkinsStatusCollector());
            collectorRegistry.register(
                    new DropwizardExports(Metrics.metricRegistry(), new JenkinsNodeBuildsSampleBuilder()));
            collectorRegistry.register(new DiskUsageCollector());
            collectorRegistry.register(new ExecutorCollector());
            collectorRegistry.register(new CodeCoverageCollector());
            // other collectors from other plugins
            ExtensionList.lookup(Collector.class).forEach(collectorRegistry::register);
            DefaultExports.initialize();
            initialized = true;
            initializing = false;
        }
    }

    @Override
    public String getMetrics() {
        return cachedMetrics.get();
    }

    @Override
    public void collectMetrics() {
        if(!initialized) {
            logger.debug("Not initialized");
            return;
        }
        try (StringWriter buffer = new StringWriter()) {
            TextFormat.write004(buffer, new FilteredMetricEnumeration(collectorRegistry.metricFamilySamples().asIterator()));
            cachedMetrics.set(buffer.toString());
        } catch (IOException e) {
            logger.debug("Unable to collect metrics");
        }
    }
}
