package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Executor;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BuildLikelyStuckGaugeTest extends MockedRunCollectorTest {


    @Test
    public void testNothingCalculatedWhenRunIsNull() {
        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenJobIsNotBuilding() {
        when(mock.isBuilding()).thenReturn(false);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenNoExecutorFound() {
        when(mock.isBuilding()).thenReturn(true);
        when(mock.getExecutor()).thenReturn(null);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());
    }

    @Test
    public void testBuildIsLikelyStuck() {
        when(mock.isBuilding()).thenReturn(true);
        Executor mockedExecutor = mock(Executor.class);
        when(mockedExecutor.isLikelyStuck()).thenReturn(true);
        when(mock.getExecutor()).thenReturn(mockedExecutor);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size());
        Assertions.assertEquals(1.0, collect.get(0).samples.get(0).value);
    }

    @Test
    public void testBuildIsNotLikelyStuck() {
        when(mock.isBuilding()).thenReturn(true);
        Executor mockedExecutor = mock(Executor.class);
        when(mockedExecutor.isLikelyStuck()).thenReturn(false);
        when(mock.getExecutor()).thenReturn(mockedExecutor);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size());
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);
    }
}