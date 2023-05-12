package org.jenkinsci.plugins.prometheus.collectors;

import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

public abstract class BaseCollectorFactory {

    protected final String namespace;
    protected final String subsystem;

    public BaseCollectorFactory() {
        namespace = ConfigurationUtils.getNamespace();
        subsystem = ConfigurationUtils.getSubSystem();
    }

    protected boolean isEnabledViaConfig(CollectorType type) {
        // prepare for disable via config
        return true;
    }
}
