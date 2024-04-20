package org.jenkinsci.plugins.prometheus.util;

import io.prometheus.client.Collector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JenkinsNodeBuildsSampleBuilderTest {
    @Test
    public void masterNodeCountFormat() {
        assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds_count",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("master", "0.5"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.builds",
                        "_count",
                        List.of("quantile"),
                        List.of("0.5"),
                        0.091670452
                )
        );
    }

    @Test
    public void masterNodeHistogramFormat() {
        assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("master", "0.999"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.builds",
                        "",
                        List.of("quantile"),
                        List.of("0.999"),
                        0.091670452
                )
        );
    }

    @Test
    public void namedNodeCountFormat() {
        assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds_count",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("evil node_name.com", "0.5"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.evil node_name.com.builds",
                        "_count",
                        List.of("quantile"),
                        List.of("0.5"),
                        0.091670452
                )
        );
    }

    @Test
    public void named_node_histogram_format() {
        assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("evil node_name.com", "0.999"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.evil node_name.com.builds",
                        "",
                        List.of("quantile"),
                        List.of("0.999"),
                        0.091670452
                )
        );
    }
}
