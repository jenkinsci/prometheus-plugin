package org.jenkinsci.plugins.prometheus.rest;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import jenkins.metrics.api.Metrics;
import org.jenkinsci.plugins.prometheus.JobCollector;
import org.jenkinsci.plugins.prometheus.MetricsRequest;

@Extension
public class PrometheusAction implements UnprotectedRootAction {
    private CollectorRegistry collectorRegistry;
    private JobCollector jobCollector = new JobCollector();
    private static final String DEFAULT_ENDPOINT = "metrics";
    private String prometheusEndpoint;

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Prometheus Metrics Exporter";
    }

    @Override
    public String getUrlName() {
        prometheusEndpoint = System.getenv("PROMETHEUS_ENDPOINT");
        if (prometheusEndpoint == null || prometheusEndpoint.length() == 0) {
            prometheusEndpoint = DEFAULT_ENDPOINT;
        }
        return prometheusEndpoint;
    }

    public Object doIndex() {
        if (collectorRegistry == null) {
            collectorRegistry = CollectorRegistry.defaultRegistry;
            collectorRegistry.register(jobCollector);
            if (Metrics.metricRegistry() != null) {
                collectorRegistry.register(new DropwizardExports(Metrics.metricRegistry()));
            }
        }
        return MetricsRequest.prometheusResponse(collectorRegistry);
    }
}