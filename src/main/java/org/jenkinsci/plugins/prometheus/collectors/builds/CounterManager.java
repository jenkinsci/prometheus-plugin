package org.jenkinsci.plugins.prometheus.collectors.builds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;

import hudson.model.Run;
import io.prometheus.client.Collector;

public class CounterManager {
    private HashMap<CounterEntry, MetricCollector<Run<?, ?>, ? extends Collector>> registeredCounters;
    private static CounterManager _Manager;

    private CounterManager() {
        registeredCounters = new HashMap<CounterEntry, MetricCollector<Run<?, ?>, ? extends Collector>>();
    }

    public static CounterManager getManager() {
        if (_Manager == null) {
            _Manager = new CounterManager();
        }
        return _Manager;
    }

    private Boolean hasCounter(CounterEntry entry) {
        return registeredCounters.containsKey(entry);
    }

    public MetricCollector<Run<?, ?>, ? extends Collector> getCounter(CollectorType type, String[]labels, String prefix){
        CounterEntry entry = new CounterEntry(type, labels, prefix);
        if(hasCounter(entry)){
            return registeredCounters.get(entry);
        }
        
        var factory = new BuildCollectorFactory();
        var counterCollector = factory.createCollector(type, labels, prefix);
        registeredCounters.put(entry, counterCollector);
        return counterCollector;
    }

    private class CounterEntry {
        private String[] labels;
        private CollectorType type;
        private String prefix;

        public CounterEntry(CollectorType type, String[] labels, String prefix) {
            this.labels = labels;
            this.type = type;
            this.prefix = prefix;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;

            CounterEntry entry = (CounterEntry) obj;

            if(this.prefix != entry.prefix){
                return false;
            }

            if(this.type != entry.type){
                return false;
            }

            // Compare labels
            return Arrays.equals(labels, entry.labels);
        }

        @Override
        public int hashCode() {
            int typeHash = type != null ? type.hashCode() : 0;
            int prefixHash = prefix != null ? prefix.hashCode() : 0;
            int result = 31 * (typeHash + Arrays.hashCode(labels) + prefixHash);
            return result;
        }
    }
}
