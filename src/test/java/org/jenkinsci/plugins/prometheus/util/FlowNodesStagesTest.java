package org.jenkinsci.plugins.prometheus.util;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class FlowNodesStagesTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
    }

    private WorkflowRun run(String script, Result expected) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);
        job.setDefinition(new CpsFlowDefinition(script, true));
        return j.assertBuildStatus(expected, job.scheduleBuild2(0));
    }

    private static Map<String, StageStatus> statuses(List<Stage> stages) {
        Map<String, StageStatus> result = new LinkedHashMap<>();
        for (Stage stage : stages) {
            result.put(stage.name(), stage.status());
        }
        return result;
    }

    private static Stage byName(List<Stage> stages, String name) {
        return stages.stream().filter(s -> s.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void scriptedSequentialStages() throws Exception {
        WorkflowRun run = run("""
                stage('one') { sleep time: 100, unit: 'MILLISECONDS' }
                stage('two') { catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') { error 'boom' } }
                stage('three') { try { error 'caught' } catch (e) { echo 'handled' } }
                """, Result.UNSTABLE);

        List<Stage> stages = FlowNodes.getSortedStageNodes(run);

        assertEquals(List.of("one", "two", "three"), stages.stream().map(Stage::name).toList());
        assertEquals(Map.of(
                "one", StageStatus.SUCCESS,
                "two", StageStatus.UNSTABLE,
                "three", StageStatus.SUCCESS), statuses(stages));
        assertTrue(stages.get(0).durationMillis() >= 100, "duration was " + stages.get(0).durationMillis());
    }

    @Test
    void failedStage() throws Exception {
        WorkflowRun run = run("""
                stage('ok') { echo 'hi' }
                stage('bad') { error 'boom' }
                """, Result.FAILURE);

        assertEquals(Map.of("ok", StageStatus.SUCCESS, "bad", StageStatus.FAILED),
                statuses(FlowNodes.getSortedStageNodes(run)));
    }

    @Test
    void abortedStage() throws Exception {
        WorkflowRun run = run("stage('slow') { timeout(time: 1, unit: 'SECONDS') { sleep 10 } }", Result.ABORTED);

        assertEquals(Map.of("slow", StageStatus.ABORTED), statuses(FlowNodes.getSortedStageNodes(run)));
    }

    @ParameterizedTest
    @CsvSource({"FAILURE, FAILED", "NOT_BUILT, NOT_EXECUTED", "ABORTED, ABORTED"})
    void interruptedStageUsesInterruptionResult(String interruptResult, StageStatus expected) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);
        job.setDefinition(new CpsFlowDefinition("stage('blocked') { echo 'started'; sleep 60 }", true));
        WorkflowRun run = job.scheduleBuild2(0).waitForStart();
        j.waitForMessage("started", run);

        Result result = Result.fromString(interruptResult);
        run.getExecutor().interrupt(result);
        j.assertBuildStatus(result, j.waitForCompletion(run));

        assertEquals(Map.of("blocked", expected), statuses(FlowNodes.getSortedStageNodes(run)));
    }

    @Test
    void parentStagesReflectChildren() throws Exception {
        WorkflowRun run = run("""
                stage('outer') { stage('inner') { unstable 'meh' } }
                stage('p') { parallel one: { stage('one') { echo '1' } }, two: { stage('two') { error '2' } } }
                """, Result.FAILURE);

        List<Stage> stages = FlowNodes.getSortedStageNodes(run);

        assertEquals(Map.of(
                "outer", StageStatus.UNSTABLE,
                "inner", StageStatus.UNSTABLE,
                "p", StageStatus.FAILED,
                "one", StageStatus.SUCCESS,
                "two", StageStatus.FAILED), statuses(stages));
        assertTrue(byName(stages, "outer").durationMillis() >= byName(stages, "inner").durationMillis());
        assertTrue(byName(stages, "p").durationMillis() >= byName(stages, "two").durationMillis());
    }

    @Test
    void declarativeStages() throws Exception {
        WorkflowRun run = run("""
                pipeline {
                  agent none
                  stages {
                    stage('build') { steps { echo 'build' } }
                    stage('skipped') {
                      when { expression { false } }
                      steps { echo 'never' }
                    }
                    stage('tests') {
                      parallel {
                        stage('unit') { steps { echo 'unit' } }
                        stage('it') { steps { catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') { error 'x' } } }
                      }
                    }
                    stage('deploy') { steps { error 'boom' } }
                    stage('afterFailure') { steps { echo 'never' } }
                  }
                }
                """, Result.FAILURE);

        List<Stage> stages = FlowNodes.getSortedStageNodes(run);

        assertEquals(Map.of(
                "build", StageStatus.SUCCESS,
                "skipped", StageStatus.NOT_EXECUTED,
                "tests", StageStatus.UNSTABLE,
                "unit", StageStatus.SUCCESS,
                "it", StageStatus.UNSTABLE,
                "deploy", StageStatus.FAILED,
                "afterFailure", StageStatus.NOT_EXECUTED), statuses(stages));
        assertEquals(0, byName(stages, "skipped").durationMillis());
    }

    @Test
    void sortsNodeIdsNumerically() {
        List<Stage> stages = new ArrayList<>(List.of(
                new Stage("10", "b", StageStatus.SUCCESS, 0),
                new Stage("9", "a", StageStatus.SUCCESS, 0),
                new Stage("100", "c", StageStatus.SUCCESS, 0)));

        FlowNodes.sortInNodeIdOrder(stages);

        assertEquals(List.of("9", "10", "100"), stages.stream().map(Stage::id).toList());
    }
}
