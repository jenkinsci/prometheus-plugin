package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.util.Stage;
import org.jenkinsci.plugins.prometheus.util.StageStatus;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class StageSummary extends BuildsMetricCollector<Run<?, ?>, Summary> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageSummary.class);

    protected StageSummary(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.STAGE_SUMMARY;
    }

    @Override
    protected String getHelpText() {
        return "Summary of Jenkins build times by Job and Stage in the last build";
    }

    @Override
    protected SimpleCollector.Builder<?, Summary> getCollectorBuilder() {
        return Summary.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> jenkinsObject, String[] labelValues) {
        if (jenkinsObject.isBuilding()) {
            return;
        }

        if (!(jenkinsObject instanceof WorkflowRun)) {
            return;
        }

        var workflowRun = (WorkflowRun) jenkinsObject;
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

        if (stage.status() == StageStatus.SUCCESS || stage.status() == StageStatus.UNSTABLE) {
            LOGGER.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.name(), run.getNumber(), job.getName());
            long duration = stage.durationMillis();
            LOGGER.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.name(), run.getNumber(), job.getName());
            collector.labels(values).observe(duration);
        } else {
            LOGGER.debug("Stage[{}] in run [{}] from job [{}] was not successful and will be ignored", stage.name(), run.getNumber(), job.getName());
        }
    }
}
