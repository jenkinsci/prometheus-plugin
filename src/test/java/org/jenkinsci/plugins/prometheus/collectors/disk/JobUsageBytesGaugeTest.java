package org.jenkinsci.plugins.prometheus.collectors.disk;

import com.cloudbees.simplediskusage.DiskItem;
import com.cloudbees.simplediskusage.JobDiskItem;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.CollectorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class JobUsageBytesGaugeTest extends CollectorTest {

    @Mock
    JobDiskItem mock;

    @Test
    public void testCollectResult() {

        when(mock.getUsage()).thenReturn(10L);

        JobUsageBytesGauge sut = new JobUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_job_usage_bytes"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Amount of disk usage (bytes) for each job in Jenkins");
        validateValue(samples, 0, 10240.0);
    }

    @Test
    public void testJobDiskItemIsNull() {
        JobUsageBytesGauge sut = new JobUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }

    @Test
    public void testJobDiskItemUsageIsNull() {
        when(mock.getUsage()).thenReturn(null);
        JobUsageBytesGauge sut = new JobUsageBytesGauge(getLabelNames(), getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);
        validateMetricFamilySampleSize(collect.get(0), 0);
    }
}