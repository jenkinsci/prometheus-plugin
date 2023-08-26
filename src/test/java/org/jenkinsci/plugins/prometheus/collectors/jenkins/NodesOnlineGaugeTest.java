package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import hudson.model.Computer;
import hudson.model.Node;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedJenkinsTest;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodesOnlineGaugeTest extends MockedJenkinsTest {


    @Test
    public void testCollectResult() throws Exception {


        setFinalStatic(Jenkins.class.getDeclaredField("VERSION"), "123");

        List<Node> nodes = new ArrayList<>();
        nodes.add(mockNode("node1", true));
        nodes.add(mockNode("node2", false));
        nodes.add(mockNode("node3", true));
        nodes.add(mockNode("nullNode", false));
        when(mock.getNodes()).thenReturn(nodes);

        NodesOnlineGauge sut = new NodesOnlineGauge(new String[]{"node"}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);
        // we pass 4 nodes but we only get 3 -> there's a node with null computer
        validateMetricFamilySampleSize(samples, 3);
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            System.out.println(sample);
            if (sample.labelValues.contains("node1")) {
                validateValue(sample, 1.0);
            }
            if (sample.labelValues.contains("node2")) {
                validateValue(sample, 0.0);
            }
            if (sample.labelValues.contains("node3")) {
                validateValue(sample, 1.0);
            }
        }
        System.out.println(samples);
        validateNames(samples, new String[]{"default_jenkins_nodes_online"});
    }

    private Node mockNode(String nodeName, boolean isOnline) {
        Node nodeMock = mock(Node.class);
        if (!"nullNode".equalsIgnoreCase(nodeName)) {
            Computer computerMock = mock(Computer.class);
            when(computerMock.isOnline()).thenReturn(isOnline);
            when(nodeMock.toComputer()).thenReturn(computerMock);
        }
        when(nodeMock.getNodeName()).thenReturn(nodeName);
        return nodeMock;
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}