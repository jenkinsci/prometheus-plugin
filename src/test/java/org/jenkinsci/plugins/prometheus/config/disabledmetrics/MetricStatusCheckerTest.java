package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class MetricStatusCheckerTest {

    @Test
    void testNoConfigWontFail() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        String namespace = "TestNamespace";

        when(mockedConfig.getDefaultNamespace()).thenReturn(namespace);
        when(mockedConfig.isCollectNodeStatus()).thenReturn(false);

        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(null);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertTrue(enabled);
        }
    }

    @Test
    void testNoDisabledMetricConfigWontFail() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        when(mockedConfig.getDisabledMetricConfig()).thenReturn(null);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertTrue(enabled);
        }
    }

    @Test
    void testNoEntriesWontFail() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        when(mockedConfig.getDisabledMetricConfig()).thenReturn(new DisabledMetricConfig(new ArrayList<>()));


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertTrue(enabled);
        }
    }

    @Test
    void testRegexMatches() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new RegexDisabledMetric("some.*"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertFalse(enabled);
        }
    }

    @Test
    void testWrongRegexWontMatch() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new RegexDisabledMetric("some*"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertTrue(enabled);
        }
    }

    @Test
    void testNamedDisabledMetricMatch() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new NamedDisabledMetric("some_metric"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertFalse(enabled);
        }
    }

    @Test
    void testNamedDisabledMetricIgnoresCaseAndMatches() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new NamedDisabledMetric("somE_mEtrIc"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertFalse(enabled);
        }
    }

    @Test
    void testNamedDisabledMetricAndRegexAvailableMatches() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new NamedDisabledMetric("other_metric"));
        entries.add(new RegexDisabledMetric("some.*"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertFalse(enabled);
        }
    }


    @Test
    void testFilterExternalMetricNames() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        entries.add(new NamedDisabledMetric("other_metric"));
        entries.add(new RegexDisabledMetric("j?vm.*"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);


        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            List<String> allMetrics = List.of("some_metric", "jvm_xxx", "vm_xxx");
            Set<String> filteredMetrics = MetricStatusChecker.filter(allMetrics);
            Assertions.assertEquals(1, filteredMetrics.size());
            Assertions.assertEquals("some_metric", filteredMetrics.stream().findFirst().get());
        }
    }

    @Test
    // shouldn't take more than 3 seconds
    @Timeout(value = 3L)
    void test10000RegexDisabledMetricConfiguredLoadTest() {
        Jenkins jenkins = mock(Jenkins.class);

        PrometheusConfiguration mockedConfig = mock(PrometheusConfiguration.class);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            entries.add(new RegexDisabledMetric("some" + i + ".*"));
        }
        // the last one matches. To see how much time it takes
        entries.add(new RegexDisabledMetric("some.*"));
        DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(entries);

        when(mockedConfig.getDisabledMetricConfig()).thenReturn(disabledMetricConfig);

        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configStatic.when(PrometheusConfiguration::get).thenReturn(mockedConfig);

            boolean enabled = MetricStatusChecker.isEnabled("some_metric");
            Assertions.assertFalse(enabled);
        }
    }
}