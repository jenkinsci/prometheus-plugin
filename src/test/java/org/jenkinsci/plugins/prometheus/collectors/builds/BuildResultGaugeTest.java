package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class BuildResultGaugeTest extends MockedRunCollectorTest {

    @Test
    public void testSuccessResultWithNoPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.SUCCESS);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_result", collect.get(0).samples.get(0).name);

    }

    @Test
    public void testSuccessResultWithPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.SUCCESS);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "last");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_last_build_result", collect.get(0).samples.get(0).name);

    }

    @Test
    public void testUnstableResultWithNoPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.UNSTABLE);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_result", collect.get(0).samples.get(0).name);

    }

    @Test
    public void testFailureResultWithNoPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.FAILURE);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_result", collect.get(0).samples.get(0).name);

    }

    @Test
    public void testNotBuiltResultWithNoPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.NOT_BUILT);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_result", collect.get(0).samples.get(0).name);

    }

    @Test
    public void testAbortedResultWithNoPrefix() {
        Mockito.when(mock.getResult()).thenReturn(Result.ABORTED);

        BuildResultGauge sut = new BuildResultGauge(getLabelNames(), getNamespace() ,getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());
        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);
        Assertions.assertEquals("default_jenkins_builds_build_result", collect.get(0).samples.get(0).name);

    }
}
