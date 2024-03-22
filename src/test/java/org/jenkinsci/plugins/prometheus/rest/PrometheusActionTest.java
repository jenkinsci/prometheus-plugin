package org.jenkinsci.plugins.prometheus.rest;

import hudson.ExtensionList;
import io.prometheus.client.exporter.common.TextFormat;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.service.PrometheusMetrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.net.HttpURLConnection.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrometheusActionTest {

    @Mock
    private Jenkins jenkins;
    @Mock
    private PrometheusConfiguration configuration;

    private MockedStatic<Jenkins> jenkinsStatic;

    @BeforeEach
    public void setUp() {
        jenkinsStatic = mockStatic(Jenkins.class);
        jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
        when(jenkins.getDescriptor(PrometheusConfiguration.class)).thenReturn(configuration);
        when(configuration.getAdditionalPath()).thenReturn("prometheus");
    }

    @AfterEach
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
        try (MockedStatic<ExtensionList> extensionListMockedStatic = mockStatic(ExtensionList.class)) {
            extensionListMockedStatic.when(() -> ExtensionList.lookupSingleton(PrometheusMetrics.class)).thenReturn(prometheusMetrics);
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

            lenient().when(response.getWriter()).thenReturn(writer);
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
            Assertions.assertEquals(stringWriter.toString(), payload);
            return this;
        }

        private AssertStaplerResponse call() throws IOException, ServletException {
            httpResponse.generateResponse(null, response, null);
            return this;
        }
    }

}
