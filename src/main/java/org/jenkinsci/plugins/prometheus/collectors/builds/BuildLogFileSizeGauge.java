package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.console.AnnotatedLargeText;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildLogFileSizeGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    protected BuildLogFileSizeGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_LOGFILE_SIZE_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Build logfile size in bytes";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (!jenkinsObject.isBuilding()) {
            AnnotatedLargeText logText = jenkinsObject.getLogText();
            long logFileSize = logText.length();

            collector.labels(labelValues).set(logFileSize);
        }
    }
}
