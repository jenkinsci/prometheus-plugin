package org.jenkinsci.plugins.prometheus;

import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.cloudbees.workflow.rest.external.StatusExt;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.metrics.jobs.BuildDiscardGauge;
import org.jenkinsci.plugins.prometheus.metrics.jobs.HealthScoreGauge;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.jenkinsci.plugins.prometheus.metrics.jobs.NbBuildsGauge;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class JobCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);
    private static final String NOT_AVAILABLE = "NA";
    private static final String UNDEFINED = "UNDEFINED";

    private Summary summary;
    private Counter jobSuccessCount;
    private Counter jobFailedCount;
    private HealthScoreGauge jobHealthScoreGauge;

    private NbBuildsGauge nbBuildsGauge;

    private BuildDiscardGauge buildDiscardGauge;


    private static class BuildMetrics {

        public Gauge jobBuildResultOrdinal;
        public Gauge jobBuildResult;
        public Gauge jobBuildStartMillis;
        public Gauge jobBuildDuration;
        public Summary stageSummary;
        public Gauge jobBuildTestsTotal;
        public Gauge jobBuildTestsSkipped;
        public Gauge jobBuildTestsFailing;

        private String buildPrefix;

        public BuildMetrics(String buildPrefix) {
            this.buildPrefix = buildPrefix;
        }

        public void initCollectors(String fullname, String subsystem, String namespace, String[] labelNameArray, String[] labelStageNameArray) {
            this.jobBuildResultOrdinal = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_result_ordinal")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Build status of a job.")
                    .create();

            this.jobBuildResult = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_result")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Build status of a job as a boolean (0 or 1)")
                    .create();

            this.jobBuildDuration = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_duration_milliseconds")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Build times in milliseconds of last build")
                    .create();

            this.jobBuildStartMillis = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_start_time_milliseconds")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Last build start timestamp in milliseconds")
                    .create();

            this.jobBuildTestsTotal = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_tests_total")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Number of total tests during the last build")
                    .create();

            this.jobBuildTestsSkipped = Gauge.build()
                    .name(fullname + "_last_build_tests_skipped")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Number of skipped tests during the last build")
                    .create();

            this.jobBuildTestsFailing = Gauge.build()
                    .name(fullname + this.buildPrefix + "_build_tests_failing")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelNameArray)
                    .help("Number of failing tests during the last build")
                    .create();

            this.stageSummary = Summary.build().name(fullname + this.buildPrefix + "_stage_duration_milliseconds_summary")
                    .subsystem(subsystem).namespace(namespace)
                    .labelNames(labelStageNameArray)
                    .help("Summary of Jenkins build times by Job and Stage in the last build")
                    .create();
        }
    }

    private final BuildMetrics lastBuildMetrics = new BuildMetrics("_last");
    private final BuildMetrics perBuildMetrics = new BuildMetrics("");

    public JobCollector() {
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting metrics for prometheus");

        String namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        String fullname = "builds";
        String subsystem = ConfigurationUtils.getSubSystem();
        String jobAttribute = PrometheusConfiguration.get().getJobAttributeName();

        String[] labelBaseNameArray = {jobAttribute, "repo", "buildable"};

        String[] labelNameArray = labelBaseNameArray;
        if (PrometheusConfiguration.get().isAppendParamLabel()) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = "parameters";
        }
        if (PrometheusConfiguration.get().isAppendStatusLabel()) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = "status";
        }

        String[] buildParameterNamesAsArray = PrometheusConfiguration.get().getLabeledBuildParameterNamesAsArray();
        for (String buildParam : buildParameterNamesAsArray) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = buildParam.trim();
        }

        String[] labelStageNameArray = Arrays.copyOf(labelBaseNameArray, labelBaseNameArray.length + 1);
        labelStageNameArray[labelBaseNameArray.length] = "stage";

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

        // Below three metrics use labelNameArray which might include the optional labels
        // of "parameters" or "status"
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

        // This metric uses "base" labels as it is just the health score reported
        // by the job object and the optional labels params and status don't make much
        // sense in this context.
        jobHealthScoreGauge = new HealthScoreGauge(labelBaseNameArray, namespace, subsystem);

        nbBuildsGauge = new NbBuildsGauge(labelBaseNameArray, namespace, subsystem);

        buildDiscardGauge = new BuildDiscardGauge(labelBaseNameArray, namespace, subsystem);

        if (PrometheusConfiguration.get().isPerBuildMetrics()) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = "number";
            perBuildMetrics.initCollectors(fullname, subsystem, namespace, labelNameArray, labelStageNameArray);
        }

        // The lastBuildMetrics are initialized with the "base" labels
        lastBuildMetrics.initCollectors(fullname, subsystem, namespace, labelBaseNameArray, labelStageNameArray);


        Jobs.forEachJob(job -> {
            try {
                if (!job.isBuildable() && processDisabledJobs) {
                    logger.debug("job [{}] is disabled", job.getFullName());
                    return;
                }
                logger.debug("Collecting metrics for job [{}]", job.getFullName());
                appendJobMetrics(job);
            } catch (IllegalArgumentException e) {
                if (!e.getMessage().contains("Incorrect number of labels")) {
                    logger.warn("Caught error when processing job [{}] error: ", job.getFullName(), e);
                } // else - ignore exception
            } catch (Exception e) {
                logger.warn("Caught error when processing job [{}] error: ", job.getFullName(), e);
            }

        });

        addSamples(samples, summary.collect(), "Adding [{}] samples from summary ({})");
        addSamples(samples, jobSuccessCount.collect(), "Adding [{}] samples from counter ({})");
        addSamples(samples, jobFailedCount.collect(), "Adding [{}] samples from counter ({})");
        addSamples(samples, jobHealthScoreGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, nbBuildsGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, buildDiscardGauge.collect(), "Adding [{}] samples from gauge ({})");

        addSamples(samples, lastBuildMetrics);
        if (PrometheusConfiguration.get().isPerBuildMetrics()) {
            addSamples(samples, perBuildMetrics);
        }

        return samples;
    }

    private void addSamples(List<MetricFamilySamples> allSamples, List<MetricFamilySamples> newSamples, String logMessage) {
        for (MetricFamilySamples metricFamilySample : newSamples) {
                int sampleCount = metricFamilySample.samples.size();
                if (sampleCount > 0) {
                    logger.debug(logMessage, sampleCount, metricFamilySample.name);
                    allSamples.addAll(newSamples);
                }
            }
    }

    private void addSamples(List<MetricFamilySamples> allSamples, BuildMetrics buildMetrics) {
        addSamples(allSamples, buildMetrics.jobBuildResultOrdinal.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildResult.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildDuration.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildStartMillis.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildTestsTotal.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildTestsSkipped.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.jobBuildTestsFailing.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.stageSummary.collect(), "Adding [{}] samples from summary ({})");
    }

    protected void appendJobMetrics(Job job) {
        boolean isAppendParamLabel = PrometheusConfiguration.get().isAppendParamLabel();
        boolean isAppendStatusLabel = PrometheusConfiguration.get().isAppendStatusLabel();
        boolean isPerBuildMetrics = PrometheusConfiguration.get().isPerBuildMetrics();
        String[] buildParameterNamesAsArray = PrometheusConfiguration.get().getLabeledBuildParameterNamesAsArray();

        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = NOT_AVAILABLE;
        }
        String[] baseLabelValueArray = {job.getFullName(), repoName, String.valueOf(job.isBuildable())};

        Run lastBuild = job.getLastBuild();
        // Never built
        if (null == lastBuild) {
            logger.debug("job [{}] never built", job.getFullName());
            return;
        }

        nbBuildsGauge.calculateMetric(job, baseLabelValueArray);
        jobHealthScoreGauge.calculateMetric(job, baseLabelValueArray);
        buildDiscardGauge.calculateMetric(job, baseLabelValueArray);

        processRun(job, lastBuild, baseLabelValueArray, lastBuildMetrics);

        Run run = lastBuild;
        while (run != null) {
            logger.debug("getting metrics for run [{}] from job [{}], include per run metrics [{}]", run.getNumber(), job.getName(), isPerBuildMetrics);
            if (Runs.includeBuildInMetrics(run)) {
                logger.debug("getting build info for run [{}] from job [{}]", run.getNumber(), job.getName());

                Result runResult = run.getResult();
                String[] labelValueArray = baseLabelValueArray;

                if (isAppendParamLabel) {
                    String params = Runs.getBuildParameters(run).entrySet().stream().map(e -> "" + e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";"));
                    labelValueArray = Arrays.copyOf(labelValueArray, labelValueArray.length + 1);
                    labelValueArray[labelValueArray.length - 1] = params;
                }
                if (isAppendStatusLabel) {
                    String resultString = UNDEFINED;
                    if (runResult != null) {
                        resultString = runResult.toString();
                    }
                    labelValueArray = Arrays.copyOf(labelValueArray, labelValueArray.length + 1);
                    labelValueArray[labelValueArray.length - 1] = run.isBuilding() ? "RUNNING" : resultString;
                }

                for (String configBuildParam : buildParameterNamesAsArray) {
                    labelValueArray = Arrays.copyOf(labelValueArray, labelValueArray.length + 1);
                    String paramValue = UNDEFINED;
                    Object paramInBuild = Runs.getBuildParameters(run).get(configBuildParam);
                    if (paramInBuild != null) {
                        paramValue = String.valueOf(paramInBuild);
                    }
                    labelValueArray[labelValueArray.length - 1] = paramValue;
                }
                long duration = run.getDuration();
                if (!run.isBuilding()) {
                    summary.labels(labelValueArray).observe(duration);
                }

                if (runResult != null && !run.isBuilding()) {
                    if (runResult.ordinal == 0 || runResult.ordinal == 1) {
                        jobSuccessCount.labels(labelValueArray).inc();
                    } else {
                        jobFailedCount.labels(labelValueArray).inc();
                    }
                }
                if (isPerBuildMetrics) {
                    labelValueArray = Arrays.copyOf(labelValueArray, labelValueArray.length + 1);
                    labelValueArray[labelValueArray.length - 1] = String.valueOf(run.getNumber());

                    processRun(job, run, labelValueArray, perBuildMetrics);
                }
            }
            run = run.getPreviousBuild();
        }
    }

    private void processRun(Job job, Run run, String[] buildLabelValueArray, BuildMetrics buildMetrics) {
        long millis;
        Result runResult;
        long duration;
        int ordinal = -1;
        duration = run.getDuration();
        millis = run.getStartTimeInMillis();
        runResult = run.getResult();
        if (null != runResult) {
            ordinal = runResult.ordinal;
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
        buildMetrics.jobBuildResultOrdinal.labels(buildLabelValueArray).set(ordinal);
        buildMetrics.jobBuildResult.labels(buildLabelValueArray).set(ordinal < 2 ? 1 : 0);

        logger.debug("Processing run [{}] from job [{}]", run.getNumber(), job.getName());

        buildMetrics.jobBuildStartMillis.labels(buildLabelValueArray).set(millis);
        if (!run.isBuilding()) {

            buildMetrics.jobBuildDuration.labels(buildLabelValueArray).set(duration);
            processRunTestsResults(run, buildLabelValueArray, buildMetrics);

            if (run instanceof WorkflowRun) {
                logger.debug("run [{}] from job [{}] is of type workflowRun", run.getNumber(), job.getName());
                WorkflowRun workflowRun = (WorkflowRun) run;
                if (workflowRun.getExecution() != null) {
                    processPipelineRunStages(job, run, workflowRun, buildMetrics.stageSummary);
                }
            }
        }
    }

    private void processRunTestsResults(Run run, String[] buildLabelValueArray, BuildMetrics buildMetrics) {
        if (PrometheusConfiguration.get().isFetchTestResults() && hasTestResults(run) && !run.isBuilding()) {
            int testsTotal = run.getAction(AbstractTestResultAction.class).getTotalCount();
            int testsFail = run.getAction(AbstractTestResultAction.class).getFailCount();
            int testsSkipped = run.getAction(AbstractTestResultAction.class).getSkipCount();

            buildMetrics.jobBuildTestsTotal.labels(buildLabelValueArray).set(testsTotal);
            buildMetrics.jobBuildTestsSkipped.labels(buildLabelValueArray).set(testsSkipped);
            buildMetrics.jobBuildTestsFailing.labels(buildLabelValueArray).set(testsFail);
        }
    }

    private void processPipelineRunStages(Job job, Run latestfinishedRun, WorkflowRun workflowRun, Summary stageSummary) {
        logger.debug("Getting the sorted stage nodes for run[{}] from job [{}]", latestfinishedRun.getNumber(), job.getName());
        List<StageNodeExt> stages = getSortedStageNodes(workflowRun);
        for (StageNodeExt stage : stages) {
            if (stage != null && stageSummary != null) {
                observeStage(job, latestfinishedRun, stage, stageSummary);
            }
        }
    }

    private void observeStage(Job job, Run run, StageNodeExt stage, Summary stageSummary) {
        logger.debug("Observing stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = NOT_AVAILABLE;
        }
        String jobName = job.getFullName();
        String stageName = stage.getName();
        String[] labelValueArray = {jobName, repoName, String.valueOf(job.isBuildable()), stageName};

        if (stage.getStatus() == StatusExt.SUCCESS || stage.getStatus() == StatusExt.UNSTABLE) {
            logger.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
            long duration = stage.getDurationMillis();
            logger.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.getName(), run.getNumber(), job.getName());
            stageSummary.labels(labelValueArray).observe(duration);
        } else {
            logger.debug("Stage[{}] in run [{}] from job [{}] was not successful and will be ignored", stage.getName(), run.getNumber(), job.getName());
        }
    }

    private boolean hasTestResults(Run<?, ?> job) {
        return job.getAction(AbstractTestResultAction.class) != null;
    }
}
