package org.jenkinsci.plugins.prometheus.collectors.builds;

import java.util.Arrays;
import java.util.HashMap;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import hudson.model.Run;
import io.prometheus.client.Collector;

/*
 * This class acts as a database to keep track of counters and return an existing counter
 * if it has already been initialized. This class is necessary due to the way the plugin handles
 * configuration changes. Changing the plugins configuration can cause the labels of a metric
 * to change. This manager compares whether it has seen a counter with a specific label before
 * and returns an existing counter if it exists. Otherwise it will return a new counter initialized at zero.
 */
public class CounterManager {
    // Keeps track of Counters we have seen.
    private HashMap<CounterEntry, MetricCollector<Run<?, ?>, ? extends Collector>> registeredCounters;

    // Static singleton instance.
    private static CounterManager manager;

    // Initialize the map
    private CounterManager() {
        registeredCounters = new HashMap<CounterEntry, MetricCollector<Run<?, ?>, ? extends Collector>>();
    }

    /*
     * Singleton instance method to get the manager.
     */
    public static CounterManager getManager() {
        if (manager == null) {
            manager = new CounterManager();
        }
        return manager;
    }

    /*
    Determine if we have seen the counter before
    returns true if so otherwise false.
    */
    private Boolean hasCounter(CounterEntry entry) {
        return registeredCounters.containsKey(entry);
    }

    /*
     * Retrives a counter or initializes a new one if it doesn't exist
     * @return Metric collector counter.
     */
    public MetricCollector<Run<?, ?>, ? extends Collector> getCounter(CollectorType type, String[]labels, String prefix){
        CounterEntry entry = new CounterEntry(type, labels, prefix);

        // If we have the counter return it.
        if(hasCounter(entry)){
            return registeredCounters.get(entry);
        }
        
        // Uses the build collector factory to initialize any new instances if necessary.
        var factory = new BuildCollectorFactory();
        var counterCollector = factory.createCollector(type, labels, prefix);

        // Add the collector to the map
        registeredCounters.put(entry, counterCollector);
        return counterCollector;
    }

    /*
     * Holds metadata about a counter to determine if we need to create a new counter.
     */
    private static class CounterEntry {
        // Labels that the counter was initialized with
        private String[] labels;

        // What collector type the counter is.
        private CollectorType type;

        // Prefix of the counter.
        private String prefix;

        // namespace of the counter
        private String namespace;

        /*
         * Creates new counter entry
         */
        public CounterEntry(CollectorType type, String[] labels, String prefix) {
            this.labels = labels;
            this.type = type;
            this.prefix = prefix;
            this.namespace = ConfigurationUtils.getNamespace();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;

            CounterEntry entry = (CounterEntry) obj;

            // Compare the prefix
            if(this.prefix != null && !this.prefix.equals(entry.prefix)){
                return false;
            }

            // Compare the entry Counter type
            if(this.type != entry.type){
                return false;
            }

            // Compare namespace values.
            if(this.namespace != null && !this.namespace.equals(entry.namespace)){
                return false;
            }

            // Compare labels
            return Arrays.equals(labels, entry.labels);
        }

        @Override
        public int hashCode() {
            int typeHash = type != null ? type.hashCode() : 0;
            int prefixHash = prefix != null ? prefix.hashCode() : 0;
            int namespaceHash = namespace != null ? namespace.hashCode() : 0;
            int result = 31 * (typeHash + Arrays.hashCode(labels) + prefixHash + namespaceHash);
            return result;
        }
    }
}
