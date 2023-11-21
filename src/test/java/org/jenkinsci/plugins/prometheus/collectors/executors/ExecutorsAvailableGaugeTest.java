package org.jenkinsci.plugins.prometheus.collectors.executors;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedLoadStatisticSnapshotTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class ExecutorsAvailableGaugeTest extends MockedLoadStatisticSnapshotTest {


    @Test
    public void testCollectResult() {

        when(mock.getAvailableExecutors()).thenReturn(10);

        ExecutorsAvailableGauge sut = new ExecutorsAvailableGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_available"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Executors Available");
        validateValue(samples, 0, 10.0);
    }

    @Test
    public void testSnapshotIsNull() {
        ExecutorsAvailableGauge sut = new ExecutorsAvailableGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }
}