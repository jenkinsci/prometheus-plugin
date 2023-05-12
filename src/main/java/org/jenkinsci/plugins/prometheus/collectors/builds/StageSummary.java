package org.jenkinsci.plugins.prometheus.collectors.builds;

import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.cloudbees.workflow.rest.external.StatusExt;
import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Summary;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class StageSummary extends BuildsMetricCollector<Run, Summary> {

    private static final String NOT_AVAILABLE = "NA";
    private static final Logger LOGGER = LoggerFactory.getLogger(StageSummary.class);

    public StageSummary(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Summary initCollector() {
        return Summary.build()
                .name(calculateName("stage_duration_milliseconds_summary"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Summary of Jenkins build times by Job and Stage in the last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (jenkinsObject.isBuilding()) {
            return;
        }

        if (!(jenkinsObject instanceof WorkflowRun)) {
            return;
        }

        var workflowRun = (WorkflowRun) jenkinsObject;
        WorkflowJob job = workflowRun.getParent();
        if (workflowRun.getExecution() != null) {
            processPipelineRunStages(job, jenkinsObject, workflowRun);
        }

    }

    private void processPipelineRunStages(Job job, Run latestfinishedRun, WorkflowRun workflowRun) {
        List<StageNodeExt> stages = getSortedStageNodes(workflowRun);
        for (StageNodeExt stage : stages) {
            if (stage != null) {
                observeStage(job, latestfinishedRun, stage);
            }
        }
    }


    private void observeStage(Job job, Run run, StageNodeExt stage) {
        LOGGER.debug("Observing stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = NOT_AVAILABLE;
        }
        String jobName = job.getFullName();
        String stageName = stage.getName();
        String[] labelValueArray = {jobName, repoName, String.valueOf(job.isBuildable()), stageName};

        if (stage.getStatus() == StatusExt.SUCCESS || stage.getStatus() == StatusExt.UNSTABLE) {
            LOGGER.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
            long duration = stage.getDurationMillis();
            LOGGER.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.getName(), run.getNumber(), job.getName());
            collector.labels(labelValueArray).observe(duration);
        } else {
            LOGGER.debug("Stage[{}] in run [{}] from job [{}] was not successful and will be ignored", stage.getName(), run.getNumber(), job.getName());
        }
    }
}
