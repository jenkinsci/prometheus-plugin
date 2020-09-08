package org.jenkinsci.plugins.prometheus;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import io.prometheus.client.Counter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.jenkinsci.plugins.prometheus.util.FlowNodes;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Collector;
import io.prometheus.client.Summary;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

public class JobCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

    private Summary summary;
    private Counter jobSuccessCount;
    private Counter jobFailedCount;
    private Gauge jobBuildResultOrdinal;
    private Gauge jobBuildResult;
    private Gauge jobStartMillis;
    private Gauge jobDuration;
    private Gauge jobScore;
    private Gauge jobTestsTotal;
    private Gauge jobTestsSkipped;
    private Gauge jobTestsFailing;
    private Summary stageSummary;

    public JobCollector() {
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting metrics for prometheus");

        String namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        Set<String> jobs = new HashSet<>();
        String fullname = "builds";
        String subsystem = ConfigurationUtils.getSubSystem();
        String jobAttribute = PrometheusConfiguration.get().getJobAttributeName();
        String[] labelNameArray = {jobAttribute, "repo"};
        String[] labelStageNameArray = {jobAttribute, "repo", "stage"};
        boolean processDisabledJobs = PrometheusConfiguration.get().isProcessingDisabledBuilds();
        boolean ignoreBuildMetrics =
                !PrometheusConfiguration.get().isCountAbortedBuilds() &&
                        !PrometheusConfiguration.get().isCountFailedBuilds() &&
                        !PrometheusConfiguration.get().isCountNotBuiltBuilds() &&
                        !PrometheusConfiguration.get().isCountSuccessfulBuilds() &&
                        !PrometheusConfiguration.get().isCountUnstableBuilds();

        if (ignoreBuildMetrics) {
            return samples;
        }

        logger.debug("getting summary of build times in milliseconds by Job");

        summary = Summary.build()
                .name(fullname + "_duration_milliseconds_summary")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Summary of Jenkins build times in milliseconds by Job")
                .create();

        jobSuccessCount = Counter.build()
                .name(fullname + "_success_build_count")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Successful build count")
                .create();

        jobFailedCount = Counter.build()
                .name(fullname + "_failed_build_count")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Failed build count")
                .create();

        jobBuildResultOrdinal = Gauge.build()
                .name(fullname + "_last_build_result_ordinal")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Build status of a job.")
                .create();

        jobBuildResult = Gauge.build()
                .name(fullname + "_last_build_result")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Build status of a job as a boolean (0 or 1)")
                .create();

        jobDuration = Gauge.build()
                .name(fullname + "_last_build_duration_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Build times in milliseconds of last build")
                .create();

        jobStartMillis = Gauge.build()
                .name(fullname + "_last_build_start_time_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Last build start timestamp in milliseconds")
                .create();

        jobScore = Gauge.build()
                .name(fullname + "_last_build_score")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Build score of last build")
                .create();

        jobTestsTotal = Gauge.build()
                .name(fullname + "_last_build_tests_total")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Number of total tests during the last build")
                .create();

        jobTestsSkipped = Gauge.build()
                .name(fullname + "_last_build_tests_skipped")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Number of skipped tests during the last build")
                .create();

        jobTestsFailing = Gauge.build()
                .name(fullname + "_last_build_tests_failing")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Number of failing tests during the last build")
                .create();

        logger.debug("getting summary of build times by Job and Stage");

        stageSummary = Summary.build().name(fullname + "_stage_duration_milliseconds_summary")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelStageNameArray)
                .help("Summary of Jenkins build times by Job and Stage")
                .create();

        Jobs.forEachJob(job -> {
            String jobName = job.getName();
            String jobFullName = job.getFullName();
            logger.debug("Determining if we are already appending metrics for job [{}]", jobName);

            if (!job.isBuildable() && processDisabledJobs) {
                logger.debug("job [{}] is disabled", jobName);
                return;
            }

            if (!jobs.add(jobFullName)) {
                logger.debug("Job [{}] is already added", jobName);
                return;
            }

            logger.debug("Job [{}] is not already added. Appending its metrics", jobName);
            appendJobMetrics(job, ignoreBuildMetrics);
        });

        addSamples(samples, summary.collect(), "Adding [{}] samples from summary");
        addSamples(samples, jobSuccessCount.collect(), "Adding [{}] samples from counter");
        addSamples(samples, jobFailedCount.collect(), "Adding [{}] samples from counter");
        addSamples(samples, jobBuildResultOrdinal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildResult.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobDuration.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobStartMillis.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobTestsTotal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobTestsSkipped.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobTestsFailing.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, stageSummary.collect(), "Adding [{}] samples from summary");

        return samples;
    }

    private void addSamples(List<MetricFamilySamples> allSamples, List<MetricFamilySamples> newSamples, String logMessage) {
        int sampleCount = newSamples.get(0).samples.size();
        if (sampleCount > 0) {
            logger.debug(logMessage, sampleCount);
            allSamples.addAll(newSamples);
        }
    }

    protected void appendJobMetrics(Job job, Boolean ignoreBuildMetrics) {
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = "NA";
        }
        String[] labelValueArray = {job.getFullName(), repoName};

        Run run = job.getLastBuild();
        // Never built
        if (null == run) {
            logger.debug("job [{}] never built", job.getFullName());
            return;
        }

        /*
         * _last_build_result _last_build_result_ordinal
         *
         * SUCCESS   0 true  - The build had no errors.
         * UNSTABLE  1 true  - The build had some errors but they were not fatal. For example, some tests failed.
         * FAILURE   2 false - The build had a fatal error.
         * NOT_BUILT 3 false - The module was not built.
         * ABORTED   4 false - The build was manually aborted.
         */
        int ordinal = -1; // running
        // Job is running
        Result runResult = run.getResult();
        if (null != runResult) {
            ordinal = runResult.ordinal;
        }
        long millis = run.getStartTimeInMillis();
        long duration = run.getDuration();
        int score = job.getBuildHealth().getScore();

        jobBuildResultOrdinal.labels(labelValueArray).set(ordinal);
        jobBuildResult.labels(labelValueArray).set(ordinal < 2 ? 1 : 0);
        jobStartMillis.labels(labelValueArray).set(millis);
        jobDuration.labels(labelValueArray).set(duration);
        jobScore.labels(labelValueArray).set(score);

        if (PrometheusConfiguration.get().isFetchTestResults() && hasTestResults(run)) {
            int testsTotal = run.getAction(AbstractTestResultAction.class).getTotalCount();
            int testsFail = run.getAction(AbstractTestResultAction.class).getFailCount();
            int testsSkipped = run.getAction(AbstractTestResultAction.class).getSkipCount();

            jobTestsTotal.labels(labelValueArray).set(testsTotal);
            jobTestsSkipped.labels(labelValueArray).set(testsSkipped);
            jobTestsFailing.labels(labelValueArray).set(testsFail);
        }

        while (run != null) {
            logger.debug("getting metrics for run [{}] from job [{}]", run.getNumber(), job.getName());
            if (Runs.includeBuildInMetrics(run)) {
                logger.debug("getting build duration for run [{}] from job [{}]", run.getNumber(), job.getName());
                long buildDuration = run.getDuration();
                logger.debug("duration is [{}] for run [{}] from job [{}]", buildDuration, run.getNumber(), job.getName());
                summary.labels(labelValueArray).observe(buildDuration);
                Result result = run.getResult();
                if (result != null) {
                    if (result.ordinal == 0 || result.ordinal == 1) {
                        jobSuccessCount.labels(labelValueArray).inc();
                    } else if (result.ordinal > 1) {
                        jobFailedCount.labels(labelValueArray).inc();
                    }
                }
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
                    } catch (NullPointerException e) {
                        // ignored
                    }
                }
            }
            run = run.getPreviousBuild();
        }
    }

    private void observeStage(Job job, Run run, FlowNode stage) {
        logger.debug("Observing stage[{}] in run [{}] from job [{}]", stage.getDisplayName(), run.getNumber(), job.getName());
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = "NA";
        }
        String jobName = job.getFullName();
        String stageName = stage.getDisplayName();
        String[] labelValueArray = {jobName, repoName, stageName};

        logger.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.getDisplayName(), run.getNumber(), job.getName());
        long duration = FlowNodes.getStageDuration(stage);
        logger.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.getDisplayName(), run.getNumber(), job.getName());
        stageSummary.labels(labelValueArray).observe(duration);
    }

    private boolean hasTestResults(Run<?, ?> job) {
        return job.getAction(AbstractTestResultAction.class) != null;
    }
}
