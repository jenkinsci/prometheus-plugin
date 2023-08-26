package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricStatusChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatusChecker.class);

    public static boolean isEnabled(String metricName) {

        PrometheusConfiguration configuration = PrometheusConfiguration.get();
        if (configuration == null) {
            LOGGER.warn("Cannot check if metric is enabled. Unable to get PrometheusConfiguration");
            return true;
        }

        DisabledMetricConfig disabledMetricConfig = configuration.getDisabledMetricConfig();
        if (disabledMetricConfig == null) {
            LOGGER.debug("Cannot check if metric is enabled. No DisabledMetricConfig.");
            return true;
        }

        List<Entry> entries = disabledMetricConfig.getEntries();
        if (entries == null || entries.isEmpty()) {
            LOGGER.debug("Cannot check if metric is enabled. No entries specified in DisabledMetricConfig.");
            return true;
        }

        for (Entry entry : entries) {
            if (entry instanceof RegexDisabledMetric) {
                Pattern pattern = Pattern.compile(((RegexDisabledMetric) entry).getRegex());
                Matcher matcher = pattern.matcher(metricName);
                if (matcher.matches()) {
                    LOGGER.debug("Metric named '{}' is disabled via Jenkins Prometheus Plugin configuration. Reason: Regex", metricName);
                    return false;
                }
            }

            if (entry instanceof NamedDisabledMetric) {
                if (metricName.equalsIgnoreCase(((NamedDisabledMetric) entry).getMetricName())) {
                    LOGGER.debug("Metric named '{}' is disabled via Jenkins Prometheus Plugin configuration. Reason: Named", metricName);
                    return false;
                }
            }
        }
        return true;
    }
}
