package org.jenkinsci.plugins.prometheus.collectors.executors;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedLoadStatisticSnapshotTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class ExecutorsBusyGaugeTest extends MockedLoadStatisticSnapshotTest {


    @Test
    public void testCollectResult() {

        when(mock.getBusyExecutors()).thenReturn(10);

        ExecutorsBusyGauge sut = new ExecutorsBusyGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_busy"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Executors Busy");
        validateValue(samples, 0, 10.0);
    }

    @Test
    public void testSnapshotIsNull() {
        ExecutorsBusyGauge sut = new ExecutorsBusyGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }
}