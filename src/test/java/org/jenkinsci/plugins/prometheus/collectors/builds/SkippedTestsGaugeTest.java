package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class SkippedTestsGaugeTest extends MockedRunCollectorTest {

    @Test
    public void testCannotExecute() {

        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            SkippedTestsGauge sut = new SkippedTestsGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

            sut.calculateMetric(mock, getLabelValues());

            List<Collector.MetricFamilySamples> collect = sut.collect();

            assertEquals(1, collect.size());
            assertEquals(0, collect.get(0).samples.size());
        }
    }

    @Test
    public void testCollectSkipped() {
        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);

            when(config.isFetchTestResults()).thenReturn(true);
            AbstractTestResultAction testResultAction = mock(AbstractTestResultAction.class);
            when(testResultAction.getSkipCount()).thenReturn(100);
            when(mock.getAction(AbstractTestResultAction.class)).thenReturn(testResultAction);
            SkippedTestsGauge sut = new SkippedTestsGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

            sut.calculateMetric(mock, getLabelValues());

            List<Collector.MetricFamilySamples> collect = sut.collect();

            assertEquals(1, collect.size());
            assertEquals(100.0, collect.get(0).samples.get(0).value, 0.0);
            Assertions.assertEquals("default_jenkins_builds_last_build_tests_skipped", collect.get(0).samples.get(0).name);
        }
    }

}