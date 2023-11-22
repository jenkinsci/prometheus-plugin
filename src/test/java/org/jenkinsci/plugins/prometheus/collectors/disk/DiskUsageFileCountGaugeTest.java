package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.CollectorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiskUsageFileCountGaugeTest extends CollectorTest {

    @Mock
    DiskItem mock;

    @Test
    public void testCollectResult() {

        when(mock.getCount()).thenReturn(10L);

        DiskUsageFileCountGauge sut = new DiskUsageFileCountGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_disk_usage_file_count"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Disk usage file count of the first level folder in JENKINS_HOME");
        validateValue(samples, 0, 10.0);
    }

    @Test
    public void testDiskItemIsNull() {
        DiskUsageFileCountGauge sut = new DiskUsageFileCountGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }
}