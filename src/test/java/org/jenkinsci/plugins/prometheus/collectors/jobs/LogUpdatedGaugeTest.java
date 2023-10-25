package org.jenkinsci.plugins.prometheus.collectors.jobs;

import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class LogUpdatedGaugeTest extends JobCollectorTest {


    @Test
    public void testBasicAttributes() {
        when(job.isLogUpdated()).thenReturn(true);

        LogUpdatedGauge sut = new LogUpdatedGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);
        validateNames(samples, new String[]{"default_jenkins_builds_job_log_updated"});
        validateMetricFamilySampleSize(samples, 1);

    }

    @Test
    public void testLogIsUpdatedReturnsOne() {

        when(job.isLogUpdated()).thenReturn(true);

        LogUpdatedGauge sut = new LogUpdatedGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();
        Collector.MetricFamilySamples samples = collect.get(0);
        validateValue(samples.samples.get(0), 1.0);
    }

    @Test
    public void testLogIsNotUpdatedReturnsZero() {

        when(job.isLogUpdated()).thenReturn(false);

        LogUpdatedGauge sut = new LogUpdatedGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();
        Collector.MetricFamilySamples samples = collect.get(0);
        validateValue(samples.samples.get(0), 0.0);
    }
}