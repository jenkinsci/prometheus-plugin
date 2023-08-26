package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildDurationSummary extends BuildsMetricCollector<Run<?, ?>, Summary> {

    protected BuildDurationSummary(String[] labelNames, String namespace, String subSystem) {
        super(labelNames, namespace, subSystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_DURATION_SUMMARY;
    }

    @Override
    protected String getHelpText() {
        return "Summary of Jenkins build times in milliseconds by Job";
    }

    @Override
    protected SimpleCollector.Builder<?, Summary> getCollectorBuilder() {
        return Summary.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (!jenkinsObject.isBuilding()) {
            long duration = jenkinsObject.getDuration();
            this.collector.labels(labelValues).observe(duration);
        }
    }
}
