package org.jenkinsci.plugins.prometheus.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {

    private static final Logger logger = LoggerFactory.getLogger(FlowNodes.class);
    private static FlowNode endNode;

    /**
     * Returns true if the given node is a stage node
     */
    public static boolean isStageNode(FlowNode node) {
        return node != null && (node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null);
    }

    /**
     * Retrieve boxed node Id from a FlowNode. Utility
     * function to make this retrieval easy and efficient.
     * @param node Node to retrieve the Id from.
     * @return Boxed node Id.
     */
    private static Integer getNodeId(final FlowNode node) {
        return Integer.valueOf(node.getId());
    }

    /**
     * Traverse through all nodes (iterative, BFS)
     * and collect the stage nodes.
     * @param nodes Nodes to traverse through to find all the stage nodes.
     * @return All stage nodes in the parent trees of the nodes list.
     */
    public static List<FlowNode> traverseTree(final List<FlowNode> nodes) {
        if (nodes == null) {
            return Collections.emptyList();
        }
        final Set<Integer> visited = new HashSet<>();
        final List<FlowNode> stageNodes = new ArrayList<>();
        for (final FlowNode node : nodes) {
            if (visited.contains(getNodeId(node))) {
                return stageNodes;
            }
            if (!visitNode(node, stageNodes, visited)) {
                return stageNodes;
            }
        }
        return stageNodes;
    }

    /**
     * Visits all the parents of the given flow node
     * using iterative breadth-first-search and builds
     * a list of all nodes encountered that meet the stage
     * node criteria.
     *
     * Aborts traversal and returns currently built list
     * if a collision in the visited set occurs.
     *
     * @param node The node to visit and start the traversal.
     * @param stageNodes List to contain the discovered stage nodes.
     * @param visited Node Ids that have already been visited.
     * @return true if traversal should continue, false if already visited node encountered (abort traversal).
     */
    private static boolean visitNode(final FlowNode node, final List<FlowNode> stageNodes, final Set<Integer> visited) {
        if (node == null) {
            return true;
        }
        final Queue<FlowNode> queue = new ArrayDeque<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            final FlowNode current = queue.poll();
            final Integer id = getNodeId(current);
            if (visited.contains(id)) {
                return false;
            }
            visited.add(id);
            if (isStageNode(current)) {
                stageNodes.add(current);
            }
            final List<FlowNode> parents = current.getParents();
            if (parents == null) {
                continue;
            }
            for (final FlowNode next : parents) {
                if (visited.contains(getNodeId(next))) {
                    return false;
                }
                queue.add(next);
            }
        }
        return true;
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

    public static List<FlowNode> getSortedStageNodes(List<FlowNode> flowNodes) {
        List<FlowNode> answer = traverseTree(flowNodes);
        sortInNodeIdOrder(answer);
        getEndNode(flowNodes);
        return answer;
    }

    private static void getEndNode(List<FlowNode> flowNodes) {
        for (FlowNode node : flowNodes) {
            if (endNode == null || Integer.parseInt(endNode.getId()) < Integer.parseInt(node.getId())) {
                endNode = node;
            }
        }
    }

    public static void sortInNodeIdOrder(List<FlowNode> answer) {
        answer.sort(Comparator.comparingInt(FlowNodes::getNodeIdNumber));
    }

    public static int getNodeIdNumber(FlowNode node) {
        String id = node.getId();
        if (id != null && id.length() > 0) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse FlowNode id [{}].", id);
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
