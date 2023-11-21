package org.jenkinsci.plugins.prometheus.collectors.executors;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedLoadStatisticSnapshotTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class ExecutorsQueueLengthGaugeTest extends MockedLoadStatisticSnapshotTest {

    @Test
    public void testCollectResult() {

        when(mock.getQueueLength()).thenReturn(10);

        ExecutorsQueueLengthGauge sut = new ExecutorsQueueLengthGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_queue_length"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Executors Queue Length");
        validateValue(samples, 0, 10.0);
    }

    @Test
    public void testSnapshotIsNull() {
        ExecutorsQueueLengthGauge sut = new ExecutorsQueueLengthGauge(getLabelNames(), getNamespace(), getSubSystem(), "");
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }

}