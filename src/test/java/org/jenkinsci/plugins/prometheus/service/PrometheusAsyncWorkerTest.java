package org.jenkinsci.plugins.prometheus.service;

import hudson.ExtensionList;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PrometheusAsyncWorkerTest {

    @Test
    public void shouldCollectMetrics() {
        // given
        PrometheusAsyncWorker asyncWorker = new PrometheusAsyncWorker();
        PrometheusMetrics metrics = new TestPrometheusMetrics();
        try (MockedStatic<ExtensionList> extensionListMockedStatic = mockStatic(ExtensionList.class)) {
            extensionListMockedStatic.when(() -> ExtensionList.lookupSingleton(PrometheusMetrics.class)).thenReturn(metrics);
            // when
            asyncWorker.execute(null);

            // then
            String actual = metrics.getMetrics();
            assertEquals("1", actual);
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

    private static class TestPrometheusMetrics implements PrometheusMetrics {
        private final AtomicReference<String> cachedMetrics = new AtomicReference<>("");
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public String getMetrics() {
            return cachedMetrics.get();
        }

        @Override
        public void collectMetrics() {
            String metrics = String.valueOf(counter.incrementAndGet());
            cachedMetrics.set(metrics);
        }

    }
}
