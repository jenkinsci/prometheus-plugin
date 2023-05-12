package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.HealthReport;
import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class HealthScoreGaugeTest extends JobCollectorTest {

    @Override
    @Test
    public void testCollectResult() {

        HealthReport healthReport = new HealthReport();
        healthReport.setScore(44);
        when(job.getBuildHealth()).thenReturn(healthReport);

        HealthScoreGauge sut = new HealthScoreGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);
        validateNames(samples, new String[]{"default_jenkins_builds_health_score"});
        validateMetricFamilySampleSize(samples, 1);
        validateValue(samples.samples.get(0), 44.0);
    }
}