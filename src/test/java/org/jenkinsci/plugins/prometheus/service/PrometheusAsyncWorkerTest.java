package org.jenkinsci.plugins.prometheus.service;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.mockito.MockedStatic;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PrometheusAsyncWorkerTest {

    @Test
    public void shouldCollectMetrics() {
        try (MockedStatic<DefaultPrometheusMetrics> defaultPrometheusMetricsMockedStatic = mockStatic(DefaultPrometheusMetrics.class)) {
            // given
            DefaultPrometheusMetrics metrics = spy(DefaultPrometheusMetrics.class);
            doNothing().when(metrics).collectMetrics();
            defaultPrometheusMetricsMockedStatic.when(DefaultPrometheusMetrics::get).thenReturn(metrics);
            PrometheusAsyncWorker asyncWorker = new PrometheusAsyncWorker();

            // when
            asyncWorker.execute(null);

            // then
            verify(metrics, times(1)).collectMetrics();
        }

    }
    @Test
    public void testConvertSecondsToMillis() {
        try (MockedStatic<PrometheusConfiguration> configurationStatic = mockStatic(PrometheusConfiguration.class)) {

            PrometheusConfiguration config = mock(PrometheusConfiguration.class);
            configurationStatic.when(PrometheusConfiguration::get).thenReturn(config);
            when(config.getCollectingMetricsPeriodInSeconds()).thenReturn(12345L);
            PrometheusAsyncWorker sut = new PrometheusAsyncWorker();
            long recurrencePeriod = sut.getRecurrencePeriod();
            assertEquals(12345000L, recurrencePeriod);
        }
    }

    @Test
    @Issue("#157")
    public void ensureLoggingLevel() {
        PrometheusAsyncWorker sut = new PrometheusAsyncWorker();
        Level level = sut.getNormalLoggingLevel();
        assertEquals(Level.FINE, level);
    }
}
