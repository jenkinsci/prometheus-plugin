package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Collector;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildCompletionListener;
import org.jenkinsci.plugins.prometheus.collectors.builds.CounterManager;
import org.jenkinsci.plugins.prometheus.collectors.builds.JobLabel;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildCompletionListener.CloseableIterator;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);
    private static final String NOT_AVAILABLE = "NA";
    private static final String UNDEFINED = "UNDEFINED";

    private MetricCollector<Run<?, ?>, ? extends Collector> summary;
    private MetricCollector<Run<?, ?>, ? extends Collector> jobSuccessCount;
    private MetricCollector<Run<?, ?>, ? extends Collector> jobFailedCount;
    private MetricCollector<Run<?, ?>, ? extends Collector> jobAbortedCount;
    private MetricCollector<Run<?, ?>, ? extends Collector> jobUnstableCount;
    private MetricCollector<Run<?, ?>, ? extends Collector> jobTotalCount;
    private MetricCollector<Job<?, ?>, ? extends Collector> jobHealthScoreGauge;
    private MetricCollector<Job<?, ?>, ? extends Collector> nbBuildsGauge;
    private MetricCollector<Job<?, ?>, ? extends Collector> buildDiscardGauge;
    private MetricCollector<Job<?, ?>, ? extends Collector> currentRunDurationGauge;
    private MetricCollector<Job<?,?>, ? extends Collector> logUpdatedGauge;

    private static class BuildMetrics {

        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildResultOrdinal;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildResult;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildStartMillis;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildDuration;
        public MetricCollector<Run<?, ?>, ? extends Collector> stageSummary;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildTestsTotal;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildTestsSkipped;
        public MetricCollector<Run<?, ?>, ? extends Collector> jobBuildTestsFailing;

        public MetricCollector<Run<?,?>, ? extends Collector> jobBuildLikelyStuck;

        private final String buildPrefix;

        public BuildMetrics(String buildPrefix) {
            this.buildPrefix = buildPrefix;
        }

        public void initCollectors(String[] labelNameArray) {
            CollectorFactory factory = new CollectorFactory();
            this.jobBuildResultOrdinal = factory.createRunCollector(CollectorType.BUILD_RESULT_ORDINAL_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildResult = factory.createRunCollector(CollectorType.BUILD_RESULT_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildDuration = factory.createRunCollector(CollectorType.BUILD_DURATION_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildStartMillis = factory.createRunCollector(CollectorType.BUILD_START_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsTotal = factory.createRunCollector(CollectorType.TOTAL_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsSkipped = factory.createRunCollector(CollectorType.SKIPPED_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.jobBuildTestsFailing = factory.createRunCollector(CollectorType.FAILED_TESTS_GAUGE, labelNameArray, buildPrefix);
            this.stageSummary = factory.createRunCollector(CollectorType.STAGE_SUMMARY, ArrayUtils.add(labelNameArray, "stage"), buildPrefix);
            this.jobBuildLikelyStuck = factory.createRunCollector(CollectorType.BUILD_LIKELY_STUCK_GAUGE, labelNameArray, buildPrefix);
        }
    }

    private final BuildMetrics lastBuildMetrics = new BuildMetrics("last");
    private final BuildMetrics perBuildMetrics = new BuildMetrics("");

    public JobCollector() {}

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

        // Below metrics use labelNameArray which might include the optional labels
        // of "parameters" or "status"
        summary = factory.createRunCollector(CollectorType.BUILD_DURATION_SUMMARY, labelNameArray, null);
        BuildCompletionListener listener = BuildCompletionListener.getInstance();
        
        // Counter manager acts as a DB to retrieve any counters that are already in memory instead of reinitializing
        // them with each iteration of collect.
        var manager = CounterManager.getManager();       
        jobSuccessCount = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, labelBaseNameArray, null);
        jobFailedCount = manager.getCounter(CollectorType.BUILD_FAILED_COUNTER, labelBaseNameArray, null);
        jobTotalCount = manager.getCounter(CollectorType.BUILD_TOTAL_COUNTER, labelBaseNameArray, null);
        jobAbortedCount = manager.getCounter(CollectorType.BUILD_ABORTED_COUNTER, labelBaseNameArray, null);
        jobUnstableCount = manager.getCounter(CollectorType.BUILD_UNSTABLE_COUNTER, labelBaseNameArray, null);

        // This is a try with resources block it ensures close is called
        // so if an exception occurs we don't reach deadlock. This is analogous to a using
        // block where dispose is called after we leave the block.
        // The closeable iterator synchronizes receiving jobs and reading the iterator
        // so we don't modify the collection while iterating.
        try (CloseableIterator<Run<?,?>> iterator = listener.iterator()) {
            // Go through each run received since the last scrape.
            while (iterator.hasNext()) {
                Run<?,?> run = iterator.next();
                Job<?,?> job = run.getParent();

                // Calculate the metrics.
                String[] labelValues = JobLabel.getBaseLabelValues(job);
                jobFailedCount.calculateMetric(run, labelValues);
                jobSuccessCount.calculateMetric(run, labelValues);
                jobTotalCount.calculateMetric(run, labelValues);
                jobAbortedCount.calculateMetric(run, labelValues);
                jobUnstableCount.calculateMetric(run,labelValues);
            }
        }

        // This metric uses "base" labels as it is just the health score reported
        // by the job object and the optional labels params and status don't make much
        // sense in this context.
        jobHealthScoreGauge = factory.createJobCollector(CollectorType.HEALTH_SCORE_GAUGE, labelBaseNameArray);

        nbBuildsGauge = factory.createJobCollector(CollectorType.NB_BUILDS_GAUGE, labelBaseNameArray);

        buildDiscardGauge = factory.createJobCollector(CollectorType.BUILD_DISCARD_GAUGE, labelBaseNameArray);

        currentRunDurationGauge = factory.createJobCollector(CollectorType.CURRENT_RUN_DURATION_GAUGE, labelBaseNameArray);

        logUpdatedGauge = factory.createJobCollector(CollectorType.JOB_LOG_UPDATED_GAUGE, labelBaseNameArray);

        if (PrometheusConfiguration.get().isPerBuildMetrics()) {
            labelNameArray = Arrays.copyOf(labelNameArray, labelNameArray.length + 1);
            labelNameArray[labelNameArray.length - 1] = "number";
            perBuildMetrics.initCollectors(labelNameArray);
        }

        // The lastBuildMetrics are initialized with the "base" labels
        lastBuildMetrics.initCollectors(labelBaseNameArray);


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
        addSamples(samples, jobAbortedCount.collect(), "Adding [{}] samples from counter ({})");
        addSamples(samples, jobUnstableCount.collect(), "Adding [{}] samples from counter ({})");
        addSamples(samples, jobTotalCount.collect(), "Adding [{}] samples from counter ({})");
        addSamples(samples, jobHealthScoreGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, nbBuildsGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, buildDiscardGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, currentRunDurationGauge.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(samples, logUpdatedGauge.collect(), "Adding [{}] samples from gauge ({})");
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
        addSamples(allSamples, buildMetrics.jobBuildLikelyStuck.collect(), "Adding [{}] samples from gauge ({})");
        addSamples(allSamples, buildMetrics.stageSummary.collect(), "Adding [{}] samples from summary ({})");
    }

    protected void appendJobMetrics(Job<?, ?> job) {
        boolean isPerBuildMetrics = PrometheusConfiguration.get().isPerBuildMetrics();
        String[] baseLabelValueArray = JobLabel.getBaseLabelValues(job);

        Run<?, ?> lastBuild = job.getLastBuild();
        // Never built
        if (null == lastBuild) {
            logger.debug("job [{}] never built", job.getFullName());
            return;
        }

        nbBuildsGauge.calculateMetric(job, baseLabelValueArray);
        jobHealthScoreGauge.calculateMetric(job, baseLabelValueArray);
        buildDiscardGauge.calculateMetric(job, baseLabelValueArray);
        currentRunDurationGauge.calculateMetric(job, baseLabelValueArray);
        logUpdatedGauge.calculateMetric(job, baseLabelValueArray);

        processRun(job, lastBuild, baseLabelValueArray, lastBuildMetrics);

        Run<?, ?> run = lastBuild;
        while (run != null) {
            logger.debug("getting metrics for run [{}] from job [{}], include per run metrics [{}]", run.getNumber(), job.getName(), isPerBuildMetrics);
            if (Runs.includeBuildInMetrics(run)) {
                logger.debug("getting build info for run [{}] from job [{}]", run.getNumber(), job.getName());
                String[] labelValueArray = JobLabel.getJobLabelVaues(job, run);

                summary.calculateMetric(run, labelValueArray);
                if (isPerBuildMetrics) {
                    labelValueArray = Arrays.copyOf(labelValueArray, labelValueArray.length + 1);
                    labelValueArray[labelValueArray.length - 1] = String.valueOf(run.getNumber());

                    processRun(job, run, labelValueArray, perBuildMetrics);
                }
            }
            run = run.getPreviousBuild();
        }
    }

    private void processRun(Job<?, ?> job, Run<?, ?> run, String[] buildLabelValueArray, BuildMetrics buildMetrics) {
        logger.debug("Processing run [{}] from job [{}]", run.getNumber(), job.getName());
        buildMetrics.jobBuildResultOrdinal.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildResult.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildStartMillis.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildDuration.calculateMetric(run, buildLabelValueArray);
        // Label values are calculated within stageSummary so we pass null here.
        buildMetrics.stageSummary.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildTestsTotal.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildTestsSkipped.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildTestsFailing.calculateMetric(run, buildLabelValueArray);
        buildMetrics.jobBuildLikelyStuck.calculateMetric(run,buildLabelValueArray);
    }

}
