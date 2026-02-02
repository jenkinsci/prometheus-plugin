package org.jenkinsci.plugins.prometheus.config;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;

@SuppressWarnings("rawtypes")
public class PrometheusConfigurationTest {

    private final PrometheusConfiguration configuration;

    public PrometheusConfigurationTest() {
        this.configuration = Mockito.mock(PrometheusConfiguration.class);
        Mockito.doNothing().when((Descriptor) configuration).load();
    }

    private static List<String> wrongMetricCollectorPeriodsProvider() {
        return Arrays.asList("0", "-1", "test", null, "100L");
    }

    @ParameterizedTest
    @MethodSource("wrongMetricCollectorPeriodsProvider")
    public void shouldGetErrorWhenNotPositiveNumber(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        Mockito.when(configuration.configure(any(StaplerRequest2.class), any())).thenCallRealMethod();
        Mockito.when(configuration.doCheckCollectingMetricsPeriodInSeconds(any())).thenCallRealMethod();
        JSONObject config = getDefaultConfig();
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);


        FormValidation formValidation = configuration.doCheckCollectingMetricsPeriodInSeconds(metricCollectorPeriod);


        assertEquals(formValidation.kind, FormValidation.Kind.ERROR);
        assertEquals(formValidation.getMessage(), "CollectingMetricsPeriodInSeconds must be a positive value");
    }

    private static List<String> correctMetricCollectorPeriodsProvider() {
        return Arrays.asList("1", "100", "5.7", String.valueOf(Integer.MAX_VALUE));
    }

    @ParameterizedTest
    @MethodSource("correctMetricCollectorPeriodsProvider")
    public void shouldReturnOk(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        Mockito.when(configuration.configure(any(StaplerRequest2.class), any())).thenCallRealMethod();
        Mockito.when(configuration.doCheckCollectingMetricsPeriodInSeconds(any())).thenCallRealMethod();
        JSONObject config = getDefaultConfig();
        StaplerRequest2 request = Mockito.mock(StaplerRequest2.class);
        Mockito.doNothing().when(request).bindJSON(any(Object.class), any(JSONObject.class));
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);

        // when
        boolean actual = configuration.configure(request, config);

        // then
        assertTrue(actual);
    }

    @Test
    public void shouldSetDefaultValue() {
        // given
        Mockito.doCallRealMethod().when(configuration).setCollectingMetricsPeriodInSeconds(anyLong());
        Mockito.when(configuration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();
        long metricCollectorPeriod = -1L;

        // when
        configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod);
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertEquals(actual, PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
    }

    @Test
    public void shouldSetValueFromEnvForCollectingMetricsPeriodInSeconds() throws Exception {
        // given
        Mockito.doCallRealMethod().when(configuration).setCollectingMetricsPeriodInSeconds(anyLong());
        Mockito.when(configuration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();
        long metricCollectorPeriod = -1L;

        // when
        withEnvironmentVariable(PrometheusConfiguration.COLLECTING_METRICS_PERIOD_IN_SECONDS, "1000")
                .execute(() -> configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod));
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertEquals(actual, 1000);
    }

    @ParameterizedTest
    @MethodSource("wrongMetricCollectorPeriodsProvider")
    public void shouldSetDefaultValueWhenEnvCannotBeConvertedToLongOrNegativeValue(String wrongValue) throws Exception {
        // given
        Mockito.doCallRealMethod().when(configuration).setCollectingMetricsPeriodInSeconds(anyLong());
        Mockito.when(configuration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();
        long metricCollectorPeriod = -1L;

        // when
        withEnvironmentVariable(PrometheusConfiguration.COLLECTING_METRICS_PERIOD_IN_SECONDS, wrongValue)
                .execute(() -> configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod));
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertEquals(actual, PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
    }

    @Test
    public void shouldTakeDefaultValueWhenNothingConfigured() {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();


        assertFalse(configuration.getCollectDiskUsage());
    }

    @Test
    public void shouldTakeEnvironmentVariableWhenNothingConfigured() throws Exception {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate constructor call
        withEnvironmentVariable(PrometheusConfiguration.COLLECT_DISK_USAGE, "false")
                .execute(configuration::setCollectDiskUsageBasedOnEnvironmentVariableIfDefined);

        assertFalse(configuration.getCollectDiskUsage());
    }

    @Test
    public void shouldTakeDefaultIfEnvironmentVariableIsFaulty() throws Exception {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate constructor call
        withEnvironmentVariable(PrometheusConfiguration.COLLECT_DISK_USAGE, "not_true_not_false")
                .execute(configuration::setCollectDiskUsageBasedOnEnvironmentVariableIfDefined);

        assertFalse(configuration.getCollectDiskUsage());
    }

    @Test
    public void shouldTakeConfiguredValueIfEnvironmentVariableIsFaulty() throws Exception {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsage(anyBoolean());

        withEnvironmentVariable(PrometheusConfiguration.COLLECT_DISK_USAGE, "not_true_not_false")
                .execute(() -> {

                    // simulate user clicked on checkbox
                    configuration.setCollectDiskUsage(true);

                    // simulate constructor call
                    configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
                });

        assertTrue(configuration.getCollectDiskUsage());
    }

    @Test
    public void shouldTakeConfiguredValueIfItIsConfigured() {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsage(anyBoolean());
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate someone configured it over the UI
        configuration.setCollectDiskUsage(false);
        assertFalse(configuration.getCollectDiskUsage());

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        assertFalse(configuration.getCollectDiskUsage());

        // simulate someone configured it over the UI
        configuration.setCollectDiskUsage(true);
        assertTrue(configuration.getCollectDiskUsage());

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        assertTrue(configuration.getCollectDiskUsage());
    }

    private JSONObject getDefaultConfig() {
        JSONObject config = new JSONObject();
        config.accumulate("path", "prometheus");
        config.accumulate("useAuthenticatedEndpoint", "true");
        config.accumulate("defaultNamespace", "default");
        config.accumulate("jobAttributeName", "jenkins_job");
        config.accumulate("countSuccessfulBuilds", "true");
        config.accumulate("countUnstableBuilds", "true");
        config.accumulate("countFailedBuilds", "true");
        config.accumulate("countNotBuiltBuilds", "true");
        config.accumulate("countAbortedBuilds", "true");
        config.accumulate("fetchTestResults", "true");
        config.accumulate("processingDisabledBuilds", "false");
        config.accumulate("appendParamLabel", "false");
        config.accumulate("appendStatusLabel", "false");
        config.accumulate("labeledBuildParameterNames", "");
        config.accumulate("collectDiskUsage", "true");
        config.accumulate("collectNodeStatus", "true");
        config.accumulate("perBuildMetrics", "false");
        config.accumulate("perBuildMetricsMaxAgeInHours", "0");
        config.accumulate("perBuildMetricsMaxBuilds", "0");
        return config;
    }

    @Test
    public void shouldSetPerBuildMetricsMaxAgeInHours() {
        // given
        Mockito.doCallRealMethod().when(configuration).setPerBuildMetricsMaxAgeInHours(Mockito.anyLong());
        Mockito.when(configuration.getPerBuildMetricsMaxAgeInHours()).thenCallRealMethod();

        // when
        configuration.setPerBuildMetricsMaxAgeInHours(24L);

        // then
        assertEquals(24L, configuration.getPerBuildMetricsMaxAgeInHours());
    }

    @Test
    public void shouldSetPerBuildMetricsMaxAgeInHoursToZeroForNegativeValues() {
        // given
        Mockito.doCallRealMethod().when(configuration).setPerBuildMetricsMaxAgeInHours(Mockito.anyLong());
        Mockito.when(configuration.getPerBuildMetricsMaxAgeInHours()).thenCallRealMethod();

        // when
        configuration.setPerBuildMetricsMaxAgeInHours(-1L);

        // then
        assertEquals(0L, configuration.getPerBuildMetricsMaxAgeInHours());
    }

    @Test
    public void shouldSetPerBuildMetricsMaxBuilds() {
        // given
        Mockito.doCallRealMethod().when(configuration).setPerBuildMetricsMaxBuilds(Mockito.anyInt());
        Mockito.when(configuration.getPerBuildMetricsMaxBuilds()).thenCallRealMethod();

        // when
        configuration.setPerBuildMetricsMaxBuilds(10);

        // then
        assertEquals(10, configuration.getPerBuildMetricsMaxBuilds());
    }

    @Test
    public void shouldSetPerBuildMetricsMaxBuildsToZeroForNegativeValues() {
        // given
        Mockito.doCallRealMethod().when(configuration).setPerBuildMetricsMaxBuilds(Mockito.anyInt());
        Mockito.when(configuration.getPerBuildMetricsMaxBuilds()).thenCallRealMethod();

        // when
        configuration.setPerBuildMetricsMaxBuilds(-5);

        // then
        assertEquals(0, configuration.getPerBuildMetricsMaxBuilds());
    }

}
