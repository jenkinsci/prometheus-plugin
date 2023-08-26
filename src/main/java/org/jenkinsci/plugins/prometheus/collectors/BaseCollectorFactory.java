package org.jenkinsci.plugins.prometheus.collectors;

import org.jenkinsci.plugins.prometheus.config.disabledmetrics.MetricStatusChecker;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

public abstract class BaseCollectorFactory {

    protected final String namespace;
    protected final String subsystem;

    public BaseCollectorFactory() {
        namespace = ConfigurationUtils.getNamespace();
        subsystem = ConfigurationUtils.getSubSystem();
    }


    protected MetricCollector saveBuildCollector(MetricCollector collector) {
        String fullName = namespace + "_" + subsystem + "_" + collector.calculateName();
        if (MetricStatusChecker.isEnabled(fullName)) {
            return collector;
        }
        return new NoOpMetricCollector<>();
    }
}
