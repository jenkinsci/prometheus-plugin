package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import io.prometheus.client.Collector;

import java.util.*;

public class FilteredMetricEnumeration implements Enumeration<Collector.MetricFamilySamples> {

    private final Iterator<Collector.MetricFamilySamples> filteredList;

    public FilteredMetricEnumeration(Iterator<Collector.MetricFamilySamples> fullList) {
        this.filteredList = filterList(fullList);
    }

    private Iterator<Collector.MetricFamilySamples> filterList(Iterator<Collector.MetricFamilySamples> fullList) {
        List<Collector.MetricFamilySamples> filteredList = new ArrayList<>();
        while (fullList.hasNext()) {
            Collector.MetricFamilySamples familySamples = fullList.next();
            if (MetricStatusChecker.isEnabled(familySamples.name)) {
                filteredList.add(familySamples);
            }
        }
        return filteredList.iterator();
    }


    @Override
    public boolean hasMoreElements() {
        return filteredList.hasNext();
    }

    @Override
    public Collector.MetricFamilySamples nextElement() {
        return filteredList.next();
    }

    @Override
    public Iterator<Collector.MetricFamilySamples> asIterator() {
        return filteredList;
    }
}
