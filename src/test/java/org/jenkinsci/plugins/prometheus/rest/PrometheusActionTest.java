package org.jenkinsci.plugins.prometheus.rest;

import io.prometheus.client.exporter.common.TextFormat;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.service.PrometheusMetrics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.net.HttpURLConnection.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrometheusActionTest {

    @Mock
    private Jenkins jenkins;
    @Mock
    private PrometheusConfiguration configuration;

    private MockedStatic<Jenkins> jenkinsStatic;

    @Before
    public void setUp() {
        jenkinsStatic = mockStatic(Jenkins.class);
        jenkinsStatic.when(() -> Jenkins.get()).thenReturn(jenkins);
        when(jenkins.getDescriptor(PrometheusConfiguration.class)).thenReturn(configuration);
        when(configuration.getAdditionalPath()).thenReturn("prometheus");
    }

    @After
    public void tearDown() {
        jenkinsStatic.close();
    }

    @Test
    public void shouldThrowExceptionWhenDoesNotMatchPath() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        StaplerRequest request = mock(StaplerRequest.class);
        String url = "";
        when(request.getRestOfPath()).thenReturn(url);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
            .call()
            .assertHttpStatus(HTTP_NOT_FOUND);
    }

    @Test
    public void shouldThrowExceptionWhenAuthenticationEnabledAndInsufficientPermission() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        StaplerRequest request = mock(StaplerRequest.class);
        String url = "prometheus";
        when(request.getRestOfPath()).thenReturn(url);
        when(configuration.isUseAuthenticatedEndpoint()).thenReturn(true);
        when(jenkins.hasPermission(Metrics.VIEW)).thenReturn(false);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
            .call()
            .assertHttpStatus(HTTP_FORBIDDEN);
    }

    @Test
    public void shouldReturnMetrics() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        PrometheusMetrics prometheusMetrics = mock(PrometheusMetrics.class);
        String responseBody = "testMetric";
        when(prometheusMetrics.getMetrics()).thenReturn(responseBody);
        action.setPrometheusMetrics(prometheusMetrics);
        StaplerRequest request = mock(StaplerRequest.class);
        String url = "prometheus";
        when(request.getRestOfPath()).thenReturn(url);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
            .call()
            .assertHttpStatus(HTTP_OK)
            .assertContentType(TextFormat.CONTENT_TYPE_004)
            .assertHttpHeader("Cache-Control", "must-revalidate,no-cache,no-store")
            .assertBody(responseBody);
    }

    private static class AssertStaplerResponse {
        private final StaplerResponse response;
        private final HttpResponse httpResponse;
        private final StringWriter stringWriter;

        private AssertStaplerResponse(HttpResponse httpResponse) throws IOException {
            this.httpResponse = httpResponse;
            this.response = mock(StaplerResponse.class);
            stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(writer);
        }

        static AssertStaplerResponse from(HttpResponse actual) throws IOException {
            return new AssertStaplerResponse(actual);
        }

        private AssertStaplerResponse assertHttpStatus(int status) {
            verify(response).setStatus(status);
            return this;
        }

        private AssertStaplerResponse assertContentType(String contentType) {
            verify(response).setContentType(contentType);
            return this;
        }

        private AssertStaplerResponse assertHttpHeader(String name, String value) {
            verify(response).addHeader(name, value);
            return this;
        }

        private AssertStaplerResponse assertBody(String payload) {
            assertThat(stringWriter).hasToString(payload);
            return this;
        }

        private AssertStaplerResponse call() throws IOException, ServletException {
            httpResponse.generateResponse(null, response, null);
            return this;
        }
    }

}
