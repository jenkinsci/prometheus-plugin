package org.jenkinsci.plugins.prometheus.service;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Extension
public class PrometheusAsyncWorker extends AsyncPeriodicWork {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusAsyncWorker.class);

    private PrometheusMetrics prometheusMetrics;

    public PrometheusAsyncWorker() {
        super("prometheus_async_worker");
    }

    @Inject
    public void setPrometheusMetrics(PrometheusMetrics prometheusMetrics) {
        this.prometheusMetrics = prometheusMetrics;
    }

    @Override
    public long getRecurrencePeriod() {
        long collectingMetricsPeriodInMillis =
                TimeUnit.SECONDS.toMillis(PrometheusConfiguration.get().getCollectingMetricsPeriodInSeconds());
        logger.debug("Setting recurrence period to {} in milliseconds", collectingMetricsPeriodInMillis);
        return collectingMetricsPeriodInMillis;
    }

    @Override
    public void execute(TaskListener taskListener) {
        logger.debug("Collecting prometheus metrics");
        prometheusMetrics.collectMetrics();
        logger.debug("Prometheus metrics collected successfully");
    }
    
    @Override
    protected Level getNormalLoggingLevel() {
        return Level.FINE;
    }

}

