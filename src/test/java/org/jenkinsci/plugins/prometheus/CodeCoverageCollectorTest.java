package org.jenkinsci.plugins.prometheus;

import hudson.Plugin;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CodeCoverageCollectorTest {

    @Mock
    private Jenkins jenkins;

    @Mock
    private PrometheusConfiguration config;

    @Test
    void shouldNotProduceMetricsWhenNoCoveragePluginDetected() {
        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class)) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            when(jenkins.getPlugin("coverage")).thenReturn(null);
            CodeCoverageCollector sut = new CodeCoverageCollector();

            List<Collector.MetricFamilySamples> collect = sut.collect();
            assertEquals(0, collect.size());
        }
    }

    @Test
    void shouldNotProduceMetricsWhenItIsNotConfigured() {
        try (
                MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
                MockedStatic<PrometheusConfiguration> configurationStatic = mockStatic(PrometheusConfiguration.class)
        ) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configurationStatic.when(PrometheusConfiguration::get).thenReturn(config);
            when(jenkins.getPlugin("coverage")).thenReturn(new Plugin.DummyImpl());
            when(config.isCollectCodeCoverage()).thenReturn(false);

            CodeCoverageCollector sut = new CodeCoverageCollector();

            List<Collector.MetricFamilySamples> collect = sut.collect();
            assertEquals(0, collect.size());
        }
    }

    @Test
    void shouldNotProduceMetricsWhenJobIsBuilding() {
        try (
                MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
                MockedStatic<PrometheusConfiguration> configurationStatic = mockStatic(PrometheusConfiguration.class);
        ) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configurationStatic.when(PrometheusConfiguration::get).thenReturn(config);
            Job jobUnderTest = mock(Job.class);
            Run lastBuild = mock(Run.class);
            when(lastBuild.isBuilding()).thenReturn(true);
            when(jobUnderTest.getLastBuild()).thenReturn(lastBuild);
            when(jenkins.getAllItems(Job.class)).thenReturn(List.of(jobUnderTest));
            when(jenkins.getPlugin("coverage")).thenReturn(new Plugin.DummyImpl());
            when(config.isCollectCodeCoverage()).thenReturn(true);

            CodeCoverageCollector sut = new CodeCoverageCollector();

            List<Collector.MetricFamilySamples> collect = sut.collect();
            assertEquals(0, collect.size());
        }
    }

    @Test
    void shouldNotProduceMetricsWhenJobHasNoCoverageBuildAction() {
        try (
                MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
                MockedStatic<PrometheusConfiguration> configurationStatic = mockStatic(PrometheusConfiguration.class);
        ) {
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configurationStatic.when(PrometheusConfiguration::get).thenReturn(config);
            Job jobUnderTest = mock(Job.class);
            Run lastBuild = mock(Run.class);
            when(lastBuild.isBuilding()).thenReturn(false);
            when(jobUnderTest.getLastBuild()).thenReturn(lastBuild);
            when(jenkins.getAllItems(Job.class)).thenReturn(List.of(jobUnderTest));
            when(jenkins.getPlugin("coverage")).thenReturn(new Plugin.DummyImpl());
            when(config.isCollectCodeCoverage()).thenReturn(true);

            CodeCoverageCollector sut = new CodeCoverageCollector();

            List<Collector.MetricFamilySamples> collect = sut.collect();
            assertEquals(0, collect.size());
        }
    }

    @Test
    void shouldProduceMetricsWhenJobHasCoverageBuildAction() {
        try (
                MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class);
                MockedStatic<PrometheusConfiguration> configurationStatic = mockStatic(PrometheusConfiguration.class);
                MockedStatic<ConfigurationUtils> configurationUtils = mockStatic(ConfigurationUtils.class);
        ) {
            configurationUtils.when(ConfigurationUtils::getNamespace).thenReturn("foo");
            configurationUtils.when(ConfigurationUtils::getSubSystem).thenReturn("bar");
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);
            configurationStatic.when(PrometheusConfiguration::get).thenReturn(config);
            Job jobUnderTest = mock(Job.class);
            Run lastBuild = mock(Run.class);
            when(lastBuild.isBuilding()).thenReturn(false);
            when(jobUnderTest.getLastBuild()).thenReturn(lastBuild);
            CoverageBuildAction action = mock(CoverageBuildAction.class);
            when(lastBuild.getAction(CoverageBuildAction.class)).thenReturn(action);
            when(jenkins.getAllItems(Job.class)).thenReturn(List.of(jobUnderTest));
            when(jenkins.getPlugin("coverage")).thenReturn(new Plugin.DummyImpl());
            when(config.getJobAttributeName()).thenReturn("jenkins_job");
            when(config.isCollectCodeCoverage()).thenReturn(true);

            CodeCoverageCollector sut = new CodeCoverageCollector();

            List<Collector.MetricFamilySamples> collect = sut.collect();
            assertEquals(12, collect.size(), "12 metrics should have been collected");
        }
    }
}