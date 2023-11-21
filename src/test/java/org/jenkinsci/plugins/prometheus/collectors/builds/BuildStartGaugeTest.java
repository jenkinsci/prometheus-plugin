package org.jenkinsci.plugins.prometheus.collectors.builds;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class BuildStartGaugeTest extends MockedRunCollectorTest {


    @Test
    public void testStartTimeGaugeWithNoPrefix() {
        Mockito.when(mock.getStartTimeInMillis()).thenReturn(1000L);

        BuildStartGauge sut = new BuildStartGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1000.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_start_time_milliseconds", collect.get(0).samples.get(0).name);
    }

    @Test
    public void testStartTimeGaugeWithPrefix() {
        Mockito.when(mock.getStartTimeInMillis()).thenReturn(1000L);

        BuildStartGauge sut = new BuildStartGauge(getLabelNames(), getNamespace(), getSubSystem(), "latest");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1000.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_latest_build_start_time_milliseconds", collect.get(0).samples.get(0).name);
    }
}