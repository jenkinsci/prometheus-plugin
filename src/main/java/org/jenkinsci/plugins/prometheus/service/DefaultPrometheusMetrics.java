package org.jenkinsci.plugins.prometheus.service;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultPrometheusMetrics implements PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPrometheusMetrics.class);

    private static DefaultPrometheusMetrics INSTANCE = null;

    private final CollectorRegistry collectorRegistry;
    private final AtomicReference<String> cachedMetrics;

    private DefaultPrometheusMetrics() {
        CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        DefaultExports.initialize();
        this.collectorRegistry = collectorRegistry;
        this.cachedMetrics = new AtomicReference<>("");
    }

    public static synchronized DefaultPrometheusMetrics get() {
        if(INSTANCE == null) {
            INSTANCE = new DefaultPrometheusMetrics();
        }
        return INSTANCE;
    }

    @Restricted(NoExternalUse.class)
    private void registerCollector(@NonNull Collector collector) {
        collectorRegistry.register(collector);
        logger.debug(String.format("Collector %s registered", collector.getClass().getName()));
    }

    @Restricted(NoExternalUse.class)
    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED, before = InitMilestone.JOB_LOADED)
    public static void registerCollectors() {
        DefaultPrometheusMetrics instance = get();
        instance.registerCollector(new JenkinsStatusCollector());
        instance.registerCollector(new DropwizardExports(Metrics.metricRegistry(), new JenkinsNodeBuildsSampleBuilder()));
        instance.registerCollector(new DiskUsageCollector());
        instance.registerCollector(new ExecutorCollector());
    }

    @Restricted(NoExternalUse.class)
    @Initializer(after = InitMilestone.JOB_LOADED, before = InitMilestone.JOB_CONFIG_ADAPTED)
    public static void registerJobCollectors() {
        DefaultPrometheusMetrics instance = get();
        instance.registerCollector(new JobCollector());
        instance.registerCollector(new CodeCoverageCollector());
        // other collectors from other plugins
        ExtensionList.lookup(Collector.class).forEach(instance::registerCollector);
    }

    @Override
    public String getMetrics() {
        return cachedMetrics.get();
    }

    @Override
    public void collectMetrics() {
        try (StringWriter buffer = new StringWriter()) {
            TextFormat.write004(buffer, new FilteredMetricEnumeration(collectorRegistry.metricFamilySamples().asIterator()));
            cachedMetrics.set(buffer.toString());
        } catch (IOException e) {
            logger.debug("Unable to collect metrics");
        }
    }
}
