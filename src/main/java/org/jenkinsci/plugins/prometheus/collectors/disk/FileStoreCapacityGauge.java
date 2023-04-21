package org.jenkinsci.plugins.prometheus.collectors.disk;

import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;

public class FileStoreCapacityGauge extends DiskMetricCollector<FileStore, Gauge> {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileStoreCapacityGauge.class);

    public FileStoreCapacityGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("file_store_capacity_bytes"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Total size in bytes of the file stores used by Jenkins")
                .create();
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
