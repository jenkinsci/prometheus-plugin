package org.jenkinsci.plugins.prometheus.metrics.builds;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.metrics.testutils.MockedRunCollectorTest;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.List;

public class BuildDurationGaugeTest extends MockedRunCollectorTest {

    @Test
    public void testCalculateDurationWhenRunIsNotBuilding() {
        Mockito.when(mock.isBuilding()).thenReturn(false);
        Mockito.when(mock.getDuration()).thenReturn(1000L);

        BuildDurationGauge sut = new BuildDurationGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();
        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1000L, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_duration_milliseconds", collect.get(0).samples.get(0).name);
    }

    @Test
    public void testCalculateDurationIsNotCalculatedWhenRunIsBuilding() {
        Mockito.when(mock.isBuilding()).thenReturn(true);

        BuildDurationGauge sut = new BuildDurationGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();
        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());
    }
}