package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedJenkinsTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class JenkinsQuietDownGaugeTest extends MockedJenkinsTest {


    @Test
    public void testCollectResultForJenkinsQuietModeEnabled() {

        when(mock.isQuietingDown()).thenReturn(true);

        JenkinsQuietDownGauge sut = new JenkinsQuietDownGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_quietdown"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins in quiet mode");
        validateValue(samples, 0, 1.0);
    }


    @Test
    public void testCollectResultForJenkinsQuietModeDisabled() {

        when(mock.isQuietingDown()).thenReturn(false);

        JenkinsQuietDownGauge sut = new JenkinsQuietDownGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_quietdown"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins in quiet mode");
        validateValue(samples, 0, 0.0);
    }

    @Test
    public void testJenkinsIsNull() {
        JenkinsQuietDownGauge sut = new JenkinsQuietDownGauge(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_quietdown"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Is Jenkins in quiet mode");
        validateValue(samples, 0, 0.0);
    }
}