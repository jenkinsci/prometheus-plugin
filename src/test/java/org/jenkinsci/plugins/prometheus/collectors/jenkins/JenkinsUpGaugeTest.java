package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import hudson.init.InitMilestone;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedJenkinsTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class JenkinsUpGaugeTest extends MockedJenkinsTest {


    @Test
    public void testCollectResultForJenkinsStarted() {

        when(mock.getInitLevel()).thenReturn(InitMilestone.STARTED);

        JenkinsUpGauge sut = new JenkinsUpGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_up"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins ready to receive requests");
        validateValue(samples, 0, 0.0);
    }


    @Test
    public void testCollectResultForJenkinsCompleted() {

        when(mock.getInitLevel()).thenReturn(InitMilestone.COMPLETED);

        JenkinsUpGauge sut = new JenkinsUpGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_up"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins ready to receive requests");
        validateValue(samples, 0, 1.0);
    }

    @Test
    public void testJenkinsIsNull() {
        JenkinsUpGauge sut = new JenkinsUpGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_up"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins ready to receive requests");
        validateValue(samples, 0, 0.0);
    }
}