package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.util.Stage;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class StageBuildResultOrdinalGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageBuildResultOrdinalGauge.class);

    protected StageBuildResultOrdinalGauge(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.STAGE_BUILDRESULT_ORDINAL;
    }

    @Override
    protected String getHelpText() {
        return "Build status of a Stage. 0=NOT_EXECUTED,1=ABORTED,2=SUCCESS,3=IN_PROGRESS,4=PAUSED_PENDING_INPUT,5=FAILED,6=UNSTABLE";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> run, String[] labelValues) {
        if (run.isBuilding()) {
            return;
        }

        if (!(run instanceof WorkflowRun)) {
            return;
        }

        var workflowRun = (WorkflowRun) run;
        WorkflowJob job = workflowRun.getParent();
        if (workflowRun.getExecution() != null) {
            processPipelineRunStages(job, workflowRun, labelValues);
        }
    }

    private void processPipelineRunStages(Job job, WorkflowRun workflowRun, String[] labelValues) {
        List<Stage> stages = getSortedStageNodes(workflowRun);
        for (Stage stage : stages) {
            if (stage != null) {
                observeStage(job, workflowRun, stage, labelValues);
            }
        }
    }

    private void observeStage(Job job, Run run, Stage stage, String[] labelValues) {

        LOGGER.debug("Observing stage[{}] in run [{}] from job [{}]", stage.name(), run.getNumber(), job.getName());
        String stageName = stage.name();

        String[] values = ArrayUtils.add(labelValues, stageName);

        collector.labels(values).set(stage.status().ordinal());
    }
}
