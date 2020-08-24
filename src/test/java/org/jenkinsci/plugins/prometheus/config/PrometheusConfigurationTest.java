package org.jenkinsci.plugins.prometheus.config;

import hudson.model.Descriptor;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(JUnitParamsRunner.class)
public class PrometheusConfigurationTest {

    private final PrometheusConfiguration configuration = new PrometheusConfiguration() {
        @Override
        public synchronized void load() {
            // This method is overridden because it calls Jenkins.get() which
            // fails because there is no Jenkins instance.
        }

        @Override
        public synchronized void save() {
            // This method is overridden because it calls Jenkins.get() which
            // fails because there is no Jenkins instance.
        }
    };

    private List<String> wrongMetricCollectorPeriodsProvider() {
        return Arrays.asList("0", "-1", "test", null, "100L");
    }

    @Test
    @Parameters(method = "wrongMetricCollectorPeriodsProvider")
    public void shouldGetErrorWhenNotPositiveNumber(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        JSONObject config = getDefaultConfig();
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);

        // when
        assertThatThrownBy(() -> configuration.configure(null, config))
                .isInstanceOf(Descriptor.FormException.class)
                .hasMessageContaining("CollectingMetricsPeriodInSeconds must be a positive integer");
    }

    private List<String> correctMetricCollectorPeriodsProvider() {
        return Arrays.asList("1", "100", "5.7", String.valueOf(Integer.MAX_VALUE));
    }

    @Test
    @Parameters(method = "correctMetricCollectorPeriodsProvider")
    public void shouldReturnOk(String metricCollectorPeriod) throws Descriptor.FormException {
        //given
        JSONObject config = getDefaultConfig();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        config.accumulate("collectingMetricsPeriodInSeconds", metricCollectorPeriod);

        // when
        boolean actual = configuration.configure(request, config);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldSetDefaultValue() {
        // given
        Long metricCollectorPeriod = null;

        // when
        configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod);
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertThat(actual).isEqualTo(PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
    }

    @Test
    public void shouldSetValueFromEnv() throws Exception{
        // given
        Long metricCollectorPeriod = null;

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
        Long metricCollectorPeriod = null;

        // when
        withEnvironmentVariable(PrometheusConfiguration.COLLECTING_METRICS_PERIOD_IN_SECONDS, wrongValue)
                .execute(() -> configuration.setCollectingMetricsPeriodInSeconds(metricCollectorPeriod));
        long actual = configuration.getCollectingMetricsPeriodInSeconds();

        // then
        assertThat(actual).isEqualTo(PrometheusConfiguration.DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
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
        return config;
    }

}
