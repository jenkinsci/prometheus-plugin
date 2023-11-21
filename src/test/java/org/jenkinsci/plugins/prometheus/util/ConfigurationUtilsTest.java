package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class ConfigurationUtilsTest {

    @Test
    public void verifyGetNamespaceWhenEnvIsNonEmpty() throws Exception {
        String namespace = "foobar";
        withEnvironmentVariable("PROMETHEUS_NAMESPACE", namespace).execute(() -> {
            String result = ConfigurationUtils.getNamespace();
            assertEquals(namespace, result);
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void verifyGetNamespaceWhenEnvIsNotSetOrEmpty(boolean notSetOrEmpty) throws Exception {
        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        String namespace = "default-namespace";
        when(config.getDefaultNamespace()).thenReturn(namespace);
        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            withEnvironmentVariable("PROMETHEUS_NAMESPACE", notSetOrEmpty ? null : "").execute(() -> {
                String result = ConfigurationUtils.getNamespace();
                assertEquals(namespace, result);
            });
        }
    }

    @Test
    public void verifyGetSubSystem() {
        assertEquals("jenkins", ConfigurationUtils.getSubSystem());
    }
}
