package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector.MetricFamilySamples;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JenkinsStatusCollectorTest {
    
    @Test
    public void shouldProduceNodeMetrics() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        String namespace = "TestNamespace";

        when(mockedConfig.getDefaultNamespace()).thenReturn(namespace);
        when(mockedConfig.isCollectNodeStatus()).thenReturn(false);

        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            JenkinsStatusCollector jenkinsStatusCollector = new JenkinsStatusCollector();

            List<MetricFamilySamples> samples = jenkinsStatusCollector.collect();
            assertEquals(3, samples.size());
        }
    }
}
