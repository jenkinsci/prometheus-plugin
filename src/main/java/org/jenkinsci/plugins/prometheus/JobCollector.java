package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.prometheus.client.Collector;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.jobs.JobCollectorFactory;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);
    private static final String NOT_AVAILABLE = "NA";
    private static final String UNDEFINED = "UNDEFINED";

    private MetricCollector<Run, ? extends Collector> summary;
    private MetricCollector<Run, ? extends Collector> jobSuccessCount;
    private MetricCollector<Run, ? extends Collector> jobFailedCount;
    private MetricCollector<Job, ? extends Collector> jobHealthScoreGauge;
    private MetricCollector<Job, ? extends Collector> nbBuildsGauge;
    private MetricCollector<Job, ? extends Collector> buildDiscardGauge;
    private MetricCollector<Job, ? extends Collector> currentRunDurationGauge;

    private static class BuildMetrics {

        public MetricCollector<Run, ? extends Collector> jobBuildResultOrdinal;
        public MetricCollector<Run, ? extends Collector> jobBuildResult;
        public MetricCollector<Run, ? extends Collector> jobBuildStartMillis;
        public MetricCollector<Run, ? extends Collector> jobBuildDuration;
        public MetricCollector<Run, ? extends Collector> stageSummary;
        public MetricCollector<Run, ? extends Collector> jobBuildTestsTotal;
        public MetricCollector<Run, ? extends Collector> jobBuildTestsSkipped;
        public MetricCollector<Run, ? extends Collector> jobBuildTestsFailing;

        private final String buildPrefix;

        public BuildMetrics(String buildPrefix) {
            this.buildPrefix = buildPrefix;
        }

        public void initCollectors(String[] labelNameArray, String[] labelStageNameArray) {
            CollectorFactory factory = new CollectorFactory();
            this.jobBuildResultOrdinal = factory.createRunCollector(CollectorType.BUILD_RESULT_ORDINAL_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildResult = factory.createRunCollector(CollectorType.BUILD_RESULT_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildDuration = factory.createRunCollector(CollectorType.BUILD_DURATION_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildStartMillis = factory.createRunCollector(CollectorType.BUILD_START_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsTotal = factory.createRunCollector(CollectorType.TOTAL_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsSkipped = factory.createRunCollector(CollectorType.SKIPPED_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsFailing = factory.createRunCollector(CollectorType.FAILED_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.stageSummary = factory.createRunCollector(CollectorType.STAGE_SUMMARY, labelStageNameArray, buildPrefix);
        }
    }

    private final BuildMetrics lastBuildMetrics = new BuildMetrics("last");
    private final BuildMetrics perBuildMetrics = new BuildMetrics("");

    public JobCollector() {
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting metrics for prometheus");

        CollectorFactory factory = new CollectorFactory();
        List<MetricFamilySamples> samples = new ArrayList<>();
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
        summary = factory.createRunCollector(CollectorType.BUILD_DURATION_SUMMARY, labelNameArray, null);

        jobSuccessCount = factory.createRunCollector(CollectorType.BUILD_SUCCESSFUL_COUNTER, labelNameArray, null);

        jobFailedCount = factory.createRunCollector(CollectorType.BUILD_FAILED_COUNTER, labelNameArray, null);

        // This metric uses "base" labels as it is just the health score reported
        // by the job object and the optional labels params and status don't make much
        // sense in this context.
        jobHealthScoreGauge = factory.createJobCollector(CollectorType.HEALTH_SCORE_GAUGE, labelBaseNameArray);

        nbBuildsGauge = factory.createJobCollector(CollectorType.NB_BUILDS_GAUGE, labelBaseNameArray);

        buildDiscardGauge = factory.createJobCollector(CollectorType.BUILD_DISCARD_GAUGE, labelBaseNameArray);

        currentRunDurationGauge = factory.createJobCollector(CollectorType.CURRENT_RUN_DURATION_GAUGE, labelBaseNameArray);

        if (PrometheusConfiguration.get().isPerBuildMetrics()) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = "number";
            perBuildMetrics.initCollectors(labelNameArray, labelStageNameArray);
        }

        // The lastBuildMetrics are initialized with the "base" labels
        lastBuildMetrics.initCollectors(labelBaseNameArray, labelStageNameArray);


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
        addSamples(samples, currentRunDurationGauge.collect(), "Adding [{}] samples from gauge ({})");
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
        currentRunDurationGauge.calculateMetric(job, baseLabelValueArray);
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

                summary.calculateMetric(run, labelValueArray);
                jobFailedCount.calculateMetric(run, labelValueArray);
                jobSuccessCount.calculateMetric(run, labelValueArray);

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
        logger.debug("Processing run [{}] from job [{}]", run.getNumber(), job.getName());
        buildMetrics.jobBuildResultOrdinal.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildResult.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildStartMillis.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildDuration.calculateMetric(run, buildLabelValueArray);
        // Label values are calculated within stageSummary so we pass null here.
        buildMetrics.stageSummary.calculateMetric(run, new String[]{});
        buildMetrics.jobBuildTestsTotal.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildTestsSkipped.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildTestsFailing.calculateMetric(run, buildLabelValueArray);
    }

}
