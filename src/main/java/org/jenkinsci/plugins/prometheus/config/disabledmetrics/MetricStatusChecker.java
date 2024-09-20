package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetricStatusChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricStatusChecker.class);

    public static boolean isEnabled(String metricName) {

        List<Entry> entries = getEntries();

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

    public static boolean isJobEnabled(String jobName) {

        List<Entry> entries = getEntries();

        for (Entry entry : entries) {
            if (entry instanceof JobRegexDisabledMetric) {
                Pattern pattern = Pattern.compile(((JobRegexDisabledMetric) entry).getRegex());
                Matcher matcher = pattern.matcher(jobName);
                if (matcher.matches()) {
                    LOGGER.debug("Job named '{}' is disabled via Jenkins Prometheus Plugin configuration. Reason: JobRegexDisabledMetric", jobName);
                    return false;
                }
            }
        }
        return true;
    }

    public static Set<String> filter(List<String> allMetricNames) {
        if (allMetricNames == null) {
            return new HashSet<>();
        }
        return allMetricNames.stream().filter(MetricStatusChecker::isEnabled).collect(Collectors.toSet());
    }

    private static List<Entry> getEntries() {
        PrometheusConfiguration configuration = PrometheusConfiguration.get();
        if (configuration == null) {
            LOGGER.warn("Cannot check if job is enabled. No PrometheusConfiguration");
            return List.of();
        }
        DisabledMetricConfig disabledMetricConfig = configuration.getDisabledMetricConfig();
        if (disabledMetricConfig == null) {
            LOGGER.debug("Cannot check if metric is enabled. No DisabledMetricConfig.");
            return List.of();
        }

        List<Entry> entries = disabledMetricConfig.getEntries();
        if (entries == null || entries.isEmpty()) {
            LOGGER.debug("Cannot check if metric is enabled. No entries specified in DisabledMetricConfig.");
            return List.of();
        }
        return entries;
    }
}
