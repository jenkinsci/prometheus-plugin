package org.jenkinsci.plugins.prometheus.collectors.disk;

import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;

public class FileStoreCapacityGauge extends BaseMetricCollector<FileStore, Gauge> {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileStoreCapacityGauge.class);

    protected FileStoreCapacityGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.FILE_STORE_CAPACITY_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Total size in bytes of the file stores used by Jenkins";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(FileStore jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        try {
            this.collector.labels(labelValues).set(jenkinsObject.getTotalSpace());
        } catch (IOException e) {
            LOGGER.debug("Failed to get total space of {}", jenkinsObject, e);
            this.collector.labels(labelValues).set(Double.NaN);
        }
    }
}
