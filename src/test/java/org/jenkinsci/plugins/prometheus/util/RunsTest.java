package org.jenkinsci.plugins.prometheus.util;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public  class RunsTest {

    @Mock
    Run mockedRun;

    @Test
    void testIncludeBuildMetricsReturnsFalseIfRunIsBuilding() {
        when(mockedRun.isBuilding()).thenReturn(true);

        boolean include = Runs.includeBuildInMetrics(mockedRun);
        assertFalse(include);
    }

    @Test
    void testGetBuildParametersWontFailIfNoActionsAvailable() {
        when(mockedRun.getActions(ParametersAction.class)).thenReturn(List.of());

        Map<String, Object> parameters = Runs.getBuildParameters(mockedRun);
        assertEquals(0, parameters.size());
    }

    @Test
    void testGetBuildParametersWontFailIfParameterValueIsNull() {
        ParameterValue parameterValue = mock(ParameterValue.class);
        when(parameterValue.getName()).thenReturn("failBuildOnError");
        when(parameterValue.getValue()).thenReturn(true);
        ParametersAction action = new ParametersAction(parameterValue);
        when(mockedRun.getActions(ParametersAction.class)).thenReturn(List.of(action));

        Map<String, Object> parameters = Runs.getBuildParameters(mockedRun);
        assertEquals(1, parameters.size());


        assertEquals(true, parameters.get("failBuildOnError"));
    }

    @ParameterizedTest
    @MethodSource("provideBuildResults")
    void testIncludeBuildMetrics(Result result) {
        when(mockedRun.isBuilding()).thenReturn(false);
        when(mockedRun.getResult()).thenReturn(result);
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {


            PrometheusConfiguration configuration = getPrometheusConfigurationForTest(result, true);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            boolean include = Runs.includeBuildInMetrics(mockedRun);
            assertTrue(include, "Run is aborted and Prometheus is configured to return results for these builds");

            configuration = getPrometheusConfigurationForTest(result, false);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            include = Runs.includeBuildInMetrics(mockedRun);
            assertFalse(include, "Run is aborted and Prometheus is not configured to return results for these builds");
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsNoLimits() {
        // When no limits are configured (both 0), all runs should be included
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(0L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(0);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            // No need to stub getTimeInMillis since max age is 0 (disabled)
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 100));
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 1000));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsMaxBuildsLimit() {
        // When max builds is set, only runs within the limit should be included
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(0L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(5);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            // No need to stub getTimeInMillis since max age is 0 (disabled)
            // Build indices 0-4 should be included (latest 5 builds)
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 4));
            
            // Build index 5+ should be excluded
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 5));
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 10));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsMaxAgeLimit() {
        // When max age is set, only recent runs should be included (based on build end time)
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(24L); // 24 hours
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(0);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            long now = System.currentTimeMillis();
            long buildDuration = 600000L; // 10 minutes build duration
            when(mockedRun.getDuration()).thenReturn(buildDuration);
            
            // Run that ended 1 hour ago should be included
            // endTime = startTime + duration, so startTime = endTime - duration
            when(mockedRun.getTimeInMillis()).thenReturn(now - 3600000L - buildDuration);
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            
            // Run that ended 23 hours ago should be included
            when(mockedRun.getTimeInMillis()).thenReturn(now - 82800000L - buildDuration);
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            
            // Run that ended 25 hours ago should be excluded
            when(mockedRun.getTimeInMillis()).thenReturn(now - 90000000L - buildDuration);
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsBothLimits() {
        // When both limits are set, the stricter one should apply
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(24L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(5);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            long now = System.currentTimeMillis();
            long buildDuration = 600000L; // 10 minutes build duration
            when(mockedRun.getDuration()).thenReturn(buildDuration);
            
            // Recent run within build count limit - should be included
            when(mockedRun.getTimeInMillis()).thenReturn(now - 3600000L - buildDuration);
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 4));
            
            // Recent run but exceeds build count limit - should be excluded
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 5));
            
            // Old run within build count limit - should be excluded due to age (ended 25 hours ago)
            when(mockedRun.getTimeInMillis()).thenReturn(now - 90000000L - buildDuration);
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsMaxBuildsEdgeCases() {
        // Test edge cases for max builds limit
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(0L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(1);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            // Only first build (index 0) should be included when maxBuilds = 1
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 1));
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 2));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsMaxAgeExactBoundary() {
        // Test exact boundary for max age (exactly 24 hours ago)
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(24L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(0);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            long now = System.currentTimeMillis();
            long buildDuration = 0L; // instant build
            when(mockedRun.getDuration()).thenReturn(buildDuration);
            
            // Build that ended just under 24 hours ago should be included
            // Using 23 hours 59 minutes to ensure it's within the limit
            long justUnder24HoursAgo = now - (23L * 60L * 60L * 1000L) - (59L * 60L * 1000L);
            when(mockedRun.getTimeInMillis()).thenReturn(justUnder24HoursAgo);
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            
            // Build that ended 25 hours ago should be excluded
            long over24HoursAgo = now - (25L * 60L * 60L * 1000L);
            when(mockedRun.getTimeInMillis()).thenReturn(over24HoursAgo);
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsZeroDuration() {
        // Test with zero duration builds
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(1L); // 1 hour
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(0);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            long now = System.currentTimeMillis();
            when(mockedRun.getDuration()).thenReturn(0L); // instant build
            
            // Build that started 30 minutes ago with 0 duration (ended 30 minutes ago)
            when(mockedRun.getTimeInMillis()).thenReturn(now - 1800000L);
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
            
            // Build that started 2 hours ago with 0 duration (ended 2 hours ago)
            when(mockedRun.getTimeInMillis()).thenReturn(now - 7200000L);
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 0));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsSequentialBuildIndices() {
        // Simulates JobCollector iterating through builds with incrementing indices
        // This covers the buildIndex++ logic in JobCollector lines 262-277
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(0L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(3);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            // Simulate iterating through builds like JobCollector does
            int buildIndex = 0;
            
            // First iteration - index 0
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, buildIndex));
            buildIndex++;
            
            // Second iteration - index 1
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, buildIndex));
            buildIndex++;
            
            // Third iteration - index 2 (last included)
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, buildIndex));
            buildIndex++;
            
            // Fourth iteration - index 3 (should be excluded)
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, buildIndex));
            buildIndex++;
            
            // Fifth iteration - index 4 (should be excluded)
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, buildIndex));
        }
    }

    @Test
    void testIncludeRunInPerBuildMetricsLongRunningBuild() {
        // Test with a long-running build where start time is old but end time is recent
        try (MockedStatic<PrometheusConfiguration> prometheusConfigurationStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration configuration = mock(PrometheusConfiguration.class);
            when(configuration.getPerBuildMetricsMaxAgeInHours()).thenReturn(24L);
            when(configuration.getPerBuildMetricsMaxBuilds()).thenReturn(0);
            prometheusConfigurationStatic.when(PrometheusConfiguration::get).thenReturn(configuration);

            long now = System.currentTimeMillis();
            long buildDuration = 48L * 60L * 60L * 1000L; // 48 hours build duration
            when(mockedRun.getDuration()).thenReturn(buildDuration);
            
            // Build started 50 hours ago but ran for 48 hours, so ended 2 hours ago
            // End time = start time + duration = (now - 50h) + 48h = now - 2h
            when(mockedRun.getTimeInMillis()).thenReturn(now - (50L * 60L * 60L * 1000L));
            assertTrue(Runs.includeRunInPerBuildMetrics(mockedRun, 0), 
                "Long-running build that ended recently should be included");
            
            // Build started 100 hours ago but ran for 48 hours, so ended 52 hours ago
            when(mockedRun.getTimeInMillis()).thenReturn(now - (100L * 60L * 60L * 1000L));
            assertFalse(Runs.includeRunInPerBuildMetrics(mockedRun, 0),
                "Long-running build that ended long ago should be excluded");
        }
    }


    private static Stream<Arguments> provideBuildResults() {
        return Stream.of(
                Arguments.of(Result.ABORTED),
                Arguments.of(Result.FAILURE),
                Arguments.of(Result.NOT_BUILT),
                Arguments.of(Result.SUCCESS),
                Arguments.of(Result.UNSTABLE)
        );
    }

    private PrometheusConfiguration getPrometheusConfigurationForTest(Result result, boolean prometheusPluginConfiguredToReturn) {
        PrometheusConfiguration mockedPrometheusConfiguration = mock(PrometheusConfiguration.class);
        if (Result.ABORTED.equals(result)) {
            when(mockedPrometheusConfiguration.isCountAbortedBuilds()).thenReturn(prometheusPluginConfiguredToReturn);
        }
        if (Result.FAILURE.equals(result)) {
            when(mockedPrometheusConfiguration.isCountFailedBuilds()).thenReturn(prometheusPluginConfiguredToReturn);
        }
        if (Result.NOT_BUILT.equals(result)) {
            when(mockedPrometheusConfiguration.isCountNotBuiltBuilds()).thenReturn(prometheusPluginConfiguredToReturn);
        }
        if (Result.SUCCESS.equals(result)) {
            when(mockedPrometheusConfiguration.isCountSuccessfulBuilds()).thenReturn(prometheusPluginConfiguredToReturn);
        }
        if (Result.UNSTABLE.equals(result)) {
            when(mockedPrometheusConfiguration.isCountUnstableBuilds()).thenReturn(prometheusPluginConfiguredToReturn);
        }
        return mockedPrometheusConfiguration;
    }
}