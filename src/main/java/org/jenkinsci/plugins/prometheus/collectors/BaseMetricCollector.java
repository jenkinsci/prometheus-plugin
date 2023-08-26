package org.jenkinsci.plugins.prometheus.collectors;

import io.prometheus.client.Collector;
import io.prometheus.client.SimpleCollector;

import java.util.List;

public abstract class BaseMetricCollector<T, I extends SimpleCollector<?>> implements MetricCollector<T, I> {

    protected final static String SEPARATOR = "_";

    protected final String[] labelNames;
    protected final String namespace;
    protected final String subsystem;
    protected final String namePrefix;
    protected I collector;

    protected BaseMetricCollector(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        this.labelNames = labelNames;
        this.namespace = namespace;
        this.subsystem = subsystem;
        this.namePrefix = namePrefix;
        collector = initCollector();
    }

    protected BaseMetricCollector(String[] labelNames, String namespace, String subsystem) {
        this.labelNames = labelNames;
        this.namespace = namespace;
        this.subsystem = subsystem;
        this.namePrefix = "";
        collector = initCollector();
    }

    /**
     * @return - the name of the collector without subsystem, namespace, prefix
     */
    protected abstract CollectorType getCollectorType();

    /**
     * @return - the help text which should be displayed
     */
    protected abstract String getHelpText();

    /**
     * @return - builder object of the  type of collector
     */
    protected abstract SimpleCollector.Builder<?, I> getCollectorBuilder();

    protected I initCollector() {
        return getCollectorBuilder()
                .name(calculateName())
                .subsystem(subsystem)
                .namespace(namespace)
                .labelNames(labelNames)
                .help(getHelpText())
                .create();
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        return collector.collect();
    }

    public String calculateName() {
        String name = getCollectorType().getName();
        StringBuilder sb = new StringBuilder();
        if (isBaseNameSet()) {
            sb.append(getBaseName()).append(SEPARATOR);
        }

        if (isNamePrefixSet()) {
            sb.append(namePrefix).append(SEPARATOR);
        }
        return sb.append(name).toString();
    }

    private boolean isBaseNameSet() {
        return getBaseName() != null && !"".equals(getBaseName());
    }

    private boolean isNamePrefixSet() {
        return namePrefix != null && !"".equals(namePrefix);
    }

    protected String getBaseName() {
        return "";
    }

    public abstract void calculateMetric(T jenkinsObject, String[] labelValues);
}
