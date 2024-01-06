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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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