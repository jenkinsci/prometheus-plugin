package org.jenkinsci.plugins.prometheus.util;

import java.util.Collections;
import java.util.Comparator;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FlowNodesTest {

    @Test
    public void getSortedStageNodesShouldReturnAnOrderedList(){
        List<FlowNode> nodeList = FlowNodeFixture.getStageNodeListOfSize(9);
        List<FlowNode> sortedNodes = getSortedStageNodes(nodeList);
        assertThat(sortedNodes).isSortedAccordingTo(Comparator.comparingInt(o -> Integer.parseInt(o.getId())));
    }

    @Test
    public void getSortedStageNodesShouldTraverseAllTheNodes(){
        List<FlowNode> nodeList = FlowNodeFixture.getStageNodeListOfSize(9);
        List<FlowNode> sortedNodes = getSortedStageNodes(nodeList);
        assertThat(sortedNodes).hasSize(9);
    }

    @Test
    public void validateAbortWhenSameNodeIsEncountered() {
        List<FlowNode> nodeList = FlowNodeFixture.getSingleStageNodeList(999);
        List<FlowNode> sortedNodes = getSortedStageNodes(nodeList);
        assertThat(sortedNodes).hasSize(1);
    }

    private static class FlowNodeFixture  {
        static List<FlowNode> getSingleStageNodeList(int nodeIndex){
            FlowNode topNode = mockStageNode(nodeIndex, Collections.emptyList());
            return Collections.singletonList(topNode);

        }

        static List<FlowNode> getStageNodeListOfSize(int numberOfNodes){
            List<FlowNode> nodeList = new ArrayList<>();
            if (numberOfNodes > 0) {
               nodeList.add(mockStageNode(numberOfNodes-1, getStageNodeListOfSize(numberOfNodes-1)));
            } else {
                return getSingleStageNodeList(numberOfNodes);
            }
            return nodeList;
        }

        private static FlowNode mockStageNode(int nodeIndex, List<FlowNode> parents) {
            FlowNode topNode = mock(FlowNode.class);
            when(topNode.getId()).thenReturn(String.valueOf(nodeIndex));
            when(topNode.getParents()).thenReturn(parents);
            when(topNode.getAction(LabelAction.class)).thenReturn(mock(LabelAction.class));
            return topNode;
        }

    }

}
