package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.CollectorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiskUsageBytesGaugeTest extends CollectorTest {

    @Mock
    DiskItem mock;

    @Test
    public void testCollectResult() {

        when(mock.getUsage()).thenReturn(10L);

        DiskUsageBytesGauge sut = new DiskUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_disk_usage_bytes"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Disk usage of first level folder in JENKINS_HOME in bytes");
        validateValue(samples, 0, 10240.0);
    }

    @Test
    public void testDiskItemIsNull() {
        DiskUsageBytesGauge sut = new DiskUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }

    @Test
    public void testDiskItemUsageIsNull() {
        when(mock.getUsage()).thenReturn(null);
        DiskUsageBytesGauge sut = new DiskUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }
}