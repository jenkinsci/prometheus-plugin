package org.jenkinsci.plugins.prometheus;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.util.Callback;
import org.jenkinsci.plugins.prometheus.util.FlowNodes;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Collector;
import io.prometheus.client.Summary;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

public class JobCollector extends Collector {
    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

    private String namespace;
    private Summary summary;
    private Summary stageSummary;

    public JobCollector() {
    	// get the namespace from the environment first
        namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (StringUtils.isEmpty(namespace)) {
        	// when the environment variable isn't set, try the system configuration
        	namespace = PrometheusConfiguration.get().getDefaultNamespace();
            logger.debug("Since the environment variable 'PROMETHEUS_NAMESPACE' is empty, using the value [{}] from the master configuration (empty strings are allowed)"+namespace);
        }
        logger.info("The prometheus namespace is [{}]", namespace);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting metrics for prometheus");
        final List<MetricFamilySamples> samples = new ArrayList<>();
        final List<Job> jobs = new ArrayList<>();
        final String fullname = "builds";
        final String subsystem = "jenkins";
        String[] labelNameArray = {"job"};
        String[] labelStageNameArray = {"job", "stage"};

        logger.debug("getting summary of build times in milliseconds by Job");
        summary = Summary.build().
                name(fullname + "_duration_milliseconds_summary").
                subsystem(subsystem).namespace(namespace).
                labelNames(labelNameArray).
                help("Summary of Jenkins build times in milliseconds by Job").
                create();

        logger.debug("getting summary of build times by Job and Stage");
        stageSummary = Summary.build().name(fullname + "_stage_duration_milliseconds_summary").
                subsystem(subsystem).namespace(namespace).
                labelNames(labelStageNameArray).
                help("Summary of Jenkins build times by Job and Stage").
                create();

        Jobs.forEachJob(new Callback<Job>() {
            @Override
            public void invoke(Job job) {
                logger.debug("Determining if we are already appending metrics for job [{}]", job.getName());
                for (Job old : jobs) {
                    if (old.getFullName().equals(job.getFullName())) {
                        // already added
                        logger.debug("Job [{}] is already added", job.getName());
                        return;
                    }
                }
                logger.debug("Job [{}] is not already added. Appending its metrics", job.getName());
                jobs.add(job);
                appendJobMetrics(job);
            }
        });
        if (summary.collect().get(0).samples.size() > 0){
            logger.debug("Adding [{}] samples from summary", summary.collect().get(0).samples.size());
            samples.addAll(summary.collect());
        }
        if (stageSummary.collect().get(0).samples.size() > 0){
            logger.debug("Adding [{}] samples from stage summary", stageSummary.collect().get(0).samples.size());
            samples.addAll(stageSummary.collect());
        }
        return samples;
    }

    protected void appendJobMetrics(Job job) {
        String[] labelValueArray = {job.getFullName()};
        Run run = job.getLastBuild();
        while (run != null) {
            logger.debug("getting metrics for run [{}] from job [{}]", run.getNumber(), job.getName());
            if (Runs.includeBuildInMetrics(run)) {
                logger.debug("getting build duration for run [{}] from job [{}]", run.getNumber(), job.getName());
                long buildDuration = run.getDuration();
                logger.debug("duration is [{}] for run [{}] from job [{}]", buildDuration, run.getNumber(), job.getName());
                summary.labels(labelValueArray).observe(buildDuration);

                if (run instanceof WorkflowRun) {
                    logger.debug("run [{}] from job [{}] is of type workflowRun", run.getNumber(), job.getName());
                    WorkflowRun workflowRun = (WorkflowRun) run;
                    if (workflowRun.getExecution() == null) {
                        run = run.getPreviousBuild();
                        continue;
                    }
                    try {
                        logger.debug("getting the sorted stage nodes for run[{}] from job [{}]", run.getNumber(), job.getName());
                        List<FlowNode> stages = getSortedStageNodes(workflowRun.getExecution());
                        for (FlowNode stage : stages) {
                            observeStage(job, run, stage);
                        }
                    } catch (final NullPointerException e){}
                }
            }
            run = run.getPreviousBuild();
        }
    }
    private void observeStage(Job job, Run run, FlowNode stage) {
        logger.debug("Observing stage[{}] in run [{}] from job [{}]", stage.getDisplayName(), run.getNumber(), job.getName());
        String jobName = job.getFullName();
        String stageName = stage.getDisplayName();
        String[] labelValueArray = {jobName, stageName};

        logger.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.getDisplayName(), run.getNumber(), job.getName());
        long duration = FlowNodes.getStageDuration(stage);
        logger.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.getDisplayName(), run.getNumber(), job.getName());
        stageSummary.labels(labelValueArray).observe(duration);
    }
}
