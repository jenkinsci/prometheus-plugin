package org.jenkinsci.plugins.prometheus;

import hudson.model.Computer;
import hudson.model.Node;
import io.prometheus.client.Collector.MetricFamilySamples;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsStatusCollectorTest {
    
    @Test
    public void shouldProduceNodeMetrics() {
        Jenkins jenkins = mock(Jenkins.class);
        Node node = mock(Node.class);
        Computer computer = mock(Computer.class);

        String namespace = "TestNamespace";
        String subsystem = "TestSubsystem";
        String nodeName = "Testnode";

        final String expectedSampleName = String.format("%s_%s_nodes_online", namespace, subsystem);

        when(jenkins.getNodes()).thenReturn(Arrays.asList(node));
        when(node.toComputer()).thenReturn(computer);
        when(node.getNodeName()).thenReturn(nodeName);
        when(computer.isOnline()).thenReturn(true);

        JenkinsStatusCollector jenkinsStatusCollector = new JenkinsStatusCollector();
        jenkinsStatusCollector.jenkins = jenkins;
        jenkinsStatusCollector.namespace = namespace;
        jenkinsStatusCollector.subsystem = subsystem;

        List<MetricFamilySamples> samples = jenkinsStatusCollector.collectNodeStatus();
        assertEquals(1, samples.size());

        MetricFamilySamples metricFamilySamples = samples.get(0);
        assertEquals(expectedSampleName, metricFamilySamples.name);

        MetricFamilySamples.Sample sample = metricFamilySamples.samples.get(0);
        assertEquals(1, sample.value, 0);
        assertEquals(1, sample.labelNames.size());
        assertEquals("node", sample.labelNames.get(0));
        assertEquals(1, sample.labelValues.size());
        assertEquals(nodeName, sample.labelValues.get(0));
    }
}
