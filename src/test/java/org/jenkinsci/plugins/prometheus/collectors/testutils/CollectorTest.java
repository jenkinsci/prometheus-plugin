package org.jenkinsci.plugins.prometheus.collectors.testutils;

import io.prometheus.client.Collector;
import org.junit.jupiter.api.Assertions;

import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class CollectorTest {

    public void validateNames(Collector.MetricFamilySamples samples, String[] expectedNames) {
        Assertions.assertArrayEquals(expectedNames, samples.getNames());
    }

    public void validateMetricFamilySampleSize(Collector.MetricFamilySamples samples, int expectedSize) {
        Assertions.assertEquals(expectedSize, samples.samples.size());
    }

    public void validateValue(Collector.MetricFamilySamples.Sample sample, double expectedValue) {
        Assertions.assertEquals(expectedValue, sample.value);
    }

    public void validateMetricFamilySampleListSize(List<Collector.MetricFamilySamples> collect, int expectedSize) {
        Assertions.assertEquals(expectedSize, collect.size());
    }

    public void validateHelp(Collector.MetricFamilySamples samples, String expectedHelp) {
        assertEquals(expectedHelp, samples.help);
    }

    public void validateValue(Collector.MetricFamilySamples samples, int index, double expectedValue) {
        assertEquals(expectedValue ,samples.samples.get(index).value, 0.0);
    }

    protected String[] getLabelNames() {
        return new String[]{"jenkins_job", "repo"};
    }

    protected String[] getLabelValues() {
        return new String[]{"job1", "NA"};
    }

    protected String getNamespace() {
        return "default";
    }

    protected String getSubSystem() {
        return "jenkins";
    }

}
