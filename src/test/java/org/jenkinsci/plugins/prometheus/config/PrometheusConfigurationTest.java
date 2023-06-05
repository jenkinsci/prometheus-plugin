package org.jenkinsci.plugins.prometheus.config;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@RunWith(JUnitParamsRunner.class)
public class PrometheusConfigurationTest {

    private PrometheusConfiguration configuration;

    @Before
    public void setup() {
        configuration = Mockito.mock(PrometheusConfiguration.class);
        Mockito.doNothing().when((Descriptor) configuration).load();
    }

    private List<String> wrongMetricCollectorPeriodsProvider() {
        return Arrays.asList("0", "-1", "test", null, "100L");
    }

    @Test
    @Parameters(method = "wrongMetricCollectorPeriodsProvider")
    public void shouldGetErrorWhenNotPositiveNumber(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        Mockito.when(configuration.configure(any(), any())).thenCallRealMethod();
        Mockito.when(configuration.doCheckCollectingMetricsPeriodInSeconds(any())).thenCallRealMethod();
        JSONObject config = getDefaultConfig();
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);


        FormValidation formValidation = configuration.doCheckCollectingMetricsPeriodInSeconds(metricCollectorPeriod);


        assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
        assertThat(formValidation.getMessage()).isEqualTo("CollectingMetricsPeriodInSeconds must be a positive value");
    }

    private List<String> correctMetricCollectorPeriodsProvider() {
        return Arrays.asList("1", "100", "5.7", String.valueOf(Integer.MAX_VALUE));
    }

    @Test
    @Parameters(method = "correctMetricCollectorPeriodsProvider")
    public void shouldReturnOk(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        Mockito.when(configuration.configure(any(), any())).thenCallRealMethod();
        JSONObject config = getDefaultConfig();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        Mockito.doNothing().when(request).bindJSON(any(Object.class), any(JSONObject.class));
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);

        // when
        boolean actual = configuration.configure(request, config);

        // then
        assertThat(actual).isTrue();
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
        assertThat(actual).isEqualTo(PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
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
        assertThat(actual).isEqualTo(1000);
    }

    @Test
    @Parameters(method = "wrongMetricCollectorPeriodsProvider")
    public void shouldSetDefaultValueWhenEnvCannotBeConvertedToLongORNegativeValue(String wrongValue) throws Exception {
        // given
        Mockito.doCallRealMethod().when(configuration).setCollectingMetricsPeriodInSeconds(anyLong());
        Mockito.when(configuration.getCollectingMetricsPeriodInSeconds()).thenCallRealMethod();
        long metricCollectorPeriod = -1L;

        // when
        withEnvironmentVariable(PrometheusConfiguration.COLLECTING_METRICS_PERIOD_IN_SECONDS, wrongValue)
                .execute(() -> configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod));
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertThat(actual).isEqualTo(PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
    }

    @Test
    public void shouldTakeDefaultValueWhenNothingConfigured() {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();


        assertThat(configuration.getCollectDiskUsage()).isFalse();
    }

    @Test
    public void shouldTakeEnvironmentVariableWhenNothingConfigured() throws Exception {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        withEnvironmentVariable(PrometheusConfiguration.COLLECT_DISK_USAGE, "false")
                .execute(() -> {
                    // simulate constructor call
                    configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
                });

        assertThat(configuration.getCollectDiskUsage()).isFalse();
    }

    @Test
    public void shouldTakeDefaultIfEnvironmentVariableIsFaulty() throws Exception {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        withEnvironmentVariable(PrometheusConfiguration.COLLECT_DISK_USAGE, "not_true_not_false")
                .execute(() -> {
                    // simulate constructor call
                    configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
                });

        assertThat(configuration.getCollectDiskUsage()).isFalse();
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

        assertThat(configuration.getCollectDiskUsage()).isTrue();
    }

    @Test
    public void shouldTakeConfiguredValueIfItIsConfigured() {
        Mockito.doCallRealMethod().when(configuration).setCollectDiskUsage(any());
        Mockito.doCallRealMethod().when(configuration).getCollectDiskUsage();

        // simulate someone configured it over the UI
        configuration.setCollectDiskUsage(false);
        assertThat(configuration.getCollectDiskUsage()).isFalse();

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        assertThat(configuration.getCollectDiskUsage()).isFalse();

        // simulate someone configured it over the UI
        configuration.setCollectDiskUsage(true);
        assertThat(configuration.getCollectDiskUsage()).isTrue();

        // simulate constructor call
        configuration.setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        assertThat(configuration.getCollectDiskUsage()).isTrue();
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
        return config;
    }

}
