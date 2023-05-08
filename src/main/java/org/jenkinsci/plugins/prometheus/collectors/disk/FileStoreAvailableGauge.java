package org.jenkinsci.plugins.prometheus.collectors.disk;

import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;

public class FileStoreAvailableGauge extends BaseMetricCollector<FileStore, Gauge> {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileStoreAvailableGauge.class);

    public FileStoreAvailableGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("file_store_available_bytes"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Estimated available space on the file stores used by Jenkins")
                .create();
    }

    @Override
    public void calculateMetric(FileStore jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        try {
            this.collector.labels(labelValues).set(jenkinsObject.getUsableSpace());
        } catch (IOException | RuntimeException e) {
            LOGGER.debug("Failed to get usable space of {}", jenkinsObject, e);
            this.collector.labels(labelValues).set(Double.NaN);
        }
    }
}
