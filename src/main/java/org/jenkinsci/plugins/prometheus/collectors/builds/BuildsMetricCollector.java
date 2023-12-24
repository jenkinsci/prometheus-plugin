package org.jenkinsci.plugins.prometheus.collectors.builds;

import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public abstract class BuildsMetricCollector<T, I extends SimpleCollector<?>> extends BaseMetricCollector<T, I> {
    protected BuildsMetricCollector(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    protected BuildsMetricCollector(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected String getBaseName() {
        return "builds";
    }
}
