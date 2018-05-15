package org.jenkinsci.plugins.prometheus.util;

import com.google.common.base.Objects;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.TreeMap;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {
    private static final Logger LOG = Logger.getLogger(FlowNodes.class.getName());
    private static FlowNode endNode;

    /**
     * Returns true if the given node is a stage node
     */
    public static boolean isStageNode(FlowNode node) {
        return node != null && node.getAction(StageAction.class) != null;
    }

    /**
     * Recursively traverses through all nodes and serializes the stage nodes
     */
    public static List<FlowNode> traverseTree(List<FlowNode> nodes, TreeMap detector) {
        final List<FlowNode> answer = new ArrayList<FlowNode>();
        if (nodes != null) {
            for (FlowNode node : nodes) {
                int id = Integer.parseInt(node.getId());
                if (detector.get(id) != null) {
                    // already added
                    return answer;
                }

                detector.put(id, true);
                if (isStageNode(node)) {
                    answer.add(node);
                }

                answer.addAll(traverseTree(node.getParents(), detector));
            }
        }
        return answer;
    }

    public static FlowNode getNextStageNode(FlowNode node) {
        List<FlowNode> list = getSortedStageNodes(node.getExecution());
        if (list.isEmpty()) {
            return null;
        }
        int idx = list.indexOf(node) + 1;
        if (idx < list.size()) {
            return list.get(idx);
        }
        // lets return the last node
        return endNode;
    }

    public static List<FlowNode> getSortedStageNodes(FlowExecution execution) {
        return getSortedStageNodes(execution.getCurrentHeads());
    }

    public static List<FlowNode> getSortedStageNodes(final List<FlowNode> flowNodes) {
        final List<FlowNode> answer = traverseTree(flowNodes, new TreeMap());
        sortInNodeIdOrder(answer);
        getEndNode(flowNodes);
        return answer;
    }

    private static void getEndNode(final List<FlowNode> flowNodes) {
        for (FlowNode node : flowNodes) {
            if (endNode == null || Integer.parseInt(endNode.getId()) < Integer.parseInt(node.getId())) {
                endNode = node;
            }
        }
    }

    public static void sortInNodeIdOrder(List<FlowNode> answer) {
        // lets sort by node id
        Comparator<? super FlowNode> comparator = new Comparator<FlowNode>() {
            @Override
            public int compare(FlowNode o1, FlowNode o2) {
                return getNodeIdNumber(o1) - getNodeIdNumber(o2);
            }
        };
        Collections.sort(answer, comparator);
    }

    public static int getNodeIdNumber(FlowNode node) {
        String id = node.getId();
        if (id != null && id.length() > 0) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                LOG.warning("Failed to parse FlowNode id " + id + ". " + e);
            }
        }
        return 0;
    }

    public static long getStageDuration(FlowNode node) {
        FlowNode nextStageNode = getNextStageNode(node);
        return getDuration(node, nextStageNode);
    }

    public static long getDuration(FlowNode startNode, FlowNode endNode) {
        long startTime = TimingAction.getStartTime(startNode);
        if (endNode != null) {
            long endTime = TimingAction.getStartTime(endNode);
            return endTime - startTime;
        }
        return 0;
    }

}
