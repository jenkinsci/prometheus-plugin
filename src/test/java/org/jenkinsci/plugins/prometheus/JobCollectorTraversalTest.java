package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.jobs.JobCollectorTest;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobCollectorTraversalTest extends JobCollectorTest {

    @Mock
    private PrometheusConfiguration config;

    private JobCollector sut;

    @BeforeEach
    void setUp() throws Exception {
        sut = new JobCollector();
        setCollectorField(sut, "nbBuildsGauge", new NoOpMetricCollector<>());
        setCollectorField(sut, "jobHealthScoreGauge", new NoOpMetricCollector<>());
        setCollectorField(sut, "buildDiscardGauge", new NoOpMetricCollector<>());
        setCollectorField(sut, "currentRunDurationGauge", new NoOpMetricCollector<>());
        setCollectorField(sut, "logUpdatedGauge", new NoOpMetricCollector<>());
        initNoOpBuildMetrics(sut, "lastBuildMetrics");
        initNoOpBuildMetrics(sut, "perBuildMetrics");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldNotTraverseBuildHistoryWhenSummaryAndPerBuildMetricsAreDisabled() throws Exception {
        setCollectorField(sut, "summary", new NoOpMetricCollector<>());

        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            when(config.isPerBuildMetrics()).thenReturn(false);

            Run lastBuild = mock(Run.class);
            when(lastBuild.isBuilding()).thenReturn(false);
            when(job.getLastBuild()).thenReturn(lastBuild);
            when(job.getFullName()).thenReturn("folder/job");

            sut.appendJobMetrics(job);

            verify(lastBuild, never()).getPreviousBuild();
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldStopTraversalAtMaxBuildsWhenSummaryIsDisabledAndPerBuildMetricsBounded() throws Exception {
        setCollectorField(sut, "summary", new NoOpMetricCollector<>());

        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            when(config.isPerBuildMetrics()).thenReturn(true);
            when(config.getPerBuildMetricsMaxBuilds()).thenReturn(1);
            when(config.isCountSuccessfulBuilds()).thenReturn(true);
            when(config.isCountFailedBuilds()).thenReturn(false);
            when(config.getLabeledBuildParameterNamesAsArray()).thenReturn(new String[0]);

            Run run3 = mock(Run.class);
            Run run2 = mock(Run.class);
            Run run1 = mock(Run.class);

            when(run3.isBuilding()).thenReturn(false);
            when(run3.getResult()).thenReturn(Result.FAILURE);
            when(run3.getNumber()).thenReturn(3);
            when(run3.getPreviousBuild()).thenReturn(run2);

            when(run2.isBuilding()).thenReturn(false);
            when(run2.getResult()).thenReturn(Result.SUCCESS);
            when(run2.getNumber()).thenReturn(2);
            lenient().when(run2.getPreviousBuild()).thenReturn(run1);

            when(job.getLastBuild()).thenReturn(run3);
            when(job.getFullName()).thenReturn("folder/job");

            sut.appendJobMetrics(job);

            verify(run3, times(1)).getPreviousBuild();
            verify(run2, never()).getPreviousBuild();
            verify(run1, never()).getPreviousBuild();
        }
    }

    private static void setCollectorField(Object target, String fieldName, MetricCollector<?, ? extends Collector> value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void initNoOpBuildMetrics(JobCollector collector, String buildMetricsFieldName) throws Exception {
        Field field = JobCollector.class.getDeclaredField(buildMetricsFieldName);
        field.setAccessible(true);
        Object buildMetrics = field.get(collector);
        for (Field metricField : buildMetrics.getClass().getDeclaredFields()) {
            if (MetricCollector.class.isAssignableFrom(metricField.getType())) {
                metricField.setAccessible(true);
                metricField.set(buildMetrics, new NoOpMetricCollector<>());
            }
        }
    }
}
