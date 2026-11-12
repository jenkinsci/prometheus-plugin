package org.jenkinsci.plugins.prometheus.util;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {

    /**
     * Tag that Declarative Pipeline puts on a stage's start node; values starting with
     * {@code SKIPPED_} mean the stage did not run (a {@code when} condition, an earlier failure, a restart).
     */
    private static final String DECLARATIVE_STAGE_STATUS_TAG = "STAGE_STATUS";

    private static final Comparator<Stage> NODE_ID_ORDER =
            Comparator.comparingInt((Stage stage) -> stage.id().length()).thenComparing(Stage::id);

    /**
     * Returns the block-scoped stages of a run (including stages nested inside other stages or
     * parallel branches), sorted by flow node id.
     */
    public static List<Stage> getSortedStageNodes(WorkflowRun run) {
        FlowExecution execution = run.getExecution();
        if (execution == null) {
            return new ArrayList<>();
        }

        Map<String, BlockStartNode> stageStarts = new HashMap<>();
        Map<String, BlockEndNode<?>> stageEnds = new HashMap<>();
        Map<String, Result> worstWarnings = new HashMap<>();

        for (FlowNode node : new DepthFirstScanner().allNodes(execution)) {
            if (isStageStart(node)) {
                stageStarts.put(node.getId(), (BlockStartNode) node);
            } else if (node instanceof BlockEndNode<?> end && isStageStart(end.getStartNode())) {
                stageEnds.put(end.getStartNode().getId(), end);
            }

            // catchError, unstable, warnError etc. record a non-fatal result on a node inside the stage
            WarningAction warning = node.getPersistentAction(WarningAction.class);
            if (warning != null) {
                for (BlockStartNode enclosing : node.getEnclosingBlocks()) {
                    if (isStageStart(enclosing)) {
                        worstWarnings.merge(enclosing.getId(), warning.getResult(), (a, b) -> a.combine(b));
                    }
                }
            }
        }

        List<Stage> answer = new ArrayList<>(stageStarts.size());
        for (BlockStartNode start : stageStarts.values()) {
            BlockEndNode<?> end = stageEnds.get(start.getId());
            StageStatus status = statusOf(start, end, worstWarnings.get(start.getId()));
            long duration = status == StageStatus.NOT_EXECUTED ? 0 : durationOf(start, end);
            answer.add(new Stage(start.getId(), start.getDisplayName(), status, duration));
        }
        sortInNodeIdOrder(answer);
        return answer;
    }

    /**
     * Sorts stages by flow node id, comparing ids numerically so that "10" comes after "9".
     */
    public static void sortInNodeIdOrder(List<Stage> answer) {
        answer.sort(NODE_ID_ORDER);
    }

    /**
     * The body of a {@code stage} step starts with a node carrying a {@link LabelAction} of the stage name.
     * Parallel branches use the {@link ThreadNameAction} subtype, which is not a stage.
     */
    private static boolean isStageStart(FlowNode node) {
        if (!(node instanceof BlockStartNode)) {
            return false;
        }
        LabelAction label = node.getPersistentAction(LabelAction.class);
        return label != null && !(label instanceof ThreadNameAction);
    }

    private static StageStatus statusOf(BlockStartNode start, BlockEndNode<?> end, Result worstWarning) {
        if (!NotExecutedNodeAction.isExecuted(start) || isSkippedByDeclarative(start)) {
            return StageStatus.NOT_EXECUTED;
        }
        if (end == null) {
            return StageStatus.IN_PROGRESS;
        }
        ErrorAction error = end.getPersistentAction(ErrorAction.class);
        if (error != null) {
            return statusOf(error.getError());
        }
        if (worstWarning != null) {
            return statusOf(worstWarning);
        }
        return StageStatus.SUCCESS;
    }

    private static boolean isSkippedByDeclarative(BlockStartNode start) {
        // Declarative tags the stage body node; check the enclosing stage step node too
        for (FlowNode node : List.of(start, start.getParents().isEmpty() ? start : start.getParents().get(0))) {
            TagsAction tags = node.getPersistentAction(TagsAction.class);
            if (tags != null) {
                String value = tags.getTagValue(DECLARATIVE_STAGE_STATUS_TAG);
                if (value != null && value.startsWith("SKIPPED_")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static StageStatus statusOf(Throwable error) {
        if (error instanceof FlowInterruptedException interrupted) {
            // isActualInterruption() only tells steps like retry whether to rethrow; the result is always getResult()
            return statusOf(interrupted.getResult());
        }
        return StageStatus.FAILED;
    }

    private static StageStatus statusOf(Result result) {
        if (result == Result.SUCCESS) {
            return StageStatus.SUCCESS;
        } else if (result == Result.UNSTABLE) {
            return StageStatus.UNSTABLE;
        } else if (result == Result.ABORTED) {
            return StageStatus.ABORTED;
        } else if (result == Result.NOT_BUILT) {
            return StageStatus.NOT_EXECUTED;
        }
        return StageStatus.FAILED;
    }

    private static long durationOf(BlockStartNode start, BlockEndNode<?> end) {
        long startTime = TimingAction.getStartTime(start);
        if (startTime == 0) {
            return 0;
        }
        long endTime = end == null ? System.currentTimeMillis() : TimingAction.getStartTime(end);
        return Math.max(0, endTime - startTime);
    }
}
