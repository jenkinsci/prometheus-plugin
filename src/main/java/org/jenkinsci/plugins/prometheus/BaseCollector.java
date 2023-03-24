package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

public abstract class BaseCollector extends Collector {


    protected static Gauge.Builder newGaugeBuilder(String... labels) {
        return newGaugeBuilder().labelNames(labels);
    }

    protected static Gauge.Builder newGaugeBuilder() {
        return Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem());
    }
}
