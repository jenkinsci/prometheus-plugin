package org.jenkinsci.plugins.prometheus.context;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.service.DefaultPrometheusMetrics;
import org.jenkinsci.plugins.prometheus.service.PrometheusMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

public class ContextTest {

    @Test
    public void testCanInjectContext() {
        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class);
             MockedStatic<Metrics> metricsStatic = mockStatic(Metrics.class)) {
            Jenkins mockedJenkins = mock(Jenkins.class);
            jenkinsStatic.when(Jenkins::get).thenReturn(mockedJenkins);

            MetricRegistry mockedMetrics = mock(MetricRegistry.class);
            metricsStatic.when(Metrics::metricRegistry).thenReturn(mockedMetrics);

            PrometheusConfiguration mockedPrometheusConfiguration = mock(PrometheusConfiguration.class);
            when(mockedPrometheusConfiguration.getLabeledBuildParameterNamesAsArray()).thenReturn(new String[]{});
            when(mockedPrometheusConfiguration.getDefaultNamespace()).thenReturn("default");
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(mockedPrometheusConfiguration);

            Injector injector = Guice.createInjector(new Context());
            PrometheusMetrics prometheusMetrics = injector.getInstance(PrometheusMetrics.class);

            Assertions.assertNotNull(prometheusMetrics);
            Assertions.assertEquals(DefaultPrometheusMetrics.class, prometheusMetrics.getClass());

            PrometheusMetrics prometheusMetrics2 = injector.getInstance(PrometheusMetrics.class);

            Assertions.assertEquals(prometheusMetrics, prometheusMetrics2, "Should be the same as it's a singleton!");


        }

    }

}