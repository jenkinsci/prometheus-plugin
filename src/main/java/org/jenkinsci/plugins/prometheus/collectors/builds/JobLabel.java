package org.jenkinsci.plugins.prometheus.collectors.builds;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.Runs;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;

/*
 * Static class that defines what labels should be added to a metric.
 */
public class JobLabel {
    private static final String NOT_AVAILABLE = "NA";
    private static final String UNDEFINED = "UNDEFINED";

    /*
     * Returns the base label names based of the Prometheus configuration
     * @return an array of label names
     */
    public static String[] getBaseLabelNames(){
        String jobAttribute = PrometheusConfiguration.get().getJobAttributeName();
        String[] labelBaseNameArray = {jobAttribute, "repo", "buildable"};

        return labelBaseNameArray;
    }

    /*
     * Returns job specific label names which appends build parameters and status
     * as a possible label.
     * @return array of label names with appropriate labels based off the prometheus config.
     */
    public static String[] getJobLabelNames(){
        String[] labelNameArray = getBaseLabelNames();
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
        return labelNameArray;
    }

    /*
     * Gets the base label values of a job. Common fields between all of the metric label values.
     * @return array of base labels.
     */
    public static String[] getBaseLabelValues(Job<?, ?> job) {
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = NOT_AVAILABLE;
        }
        String[] baseLabelValueArray = { job.getFullName(), repoName, String.valueOf(job.isBuildable()) };
        return baseLabelValueArray;
    }

    /*
     * Gets label values specific to job centric metrics.
     * @return array of label values for a job.
     */
    public static String[] getJobLabelVaues(Job<?, ?> job, Run<?, ?> run) {
        boolean isAppendParamLabel = PrometheusConfiguration.get().isAppendParamLabel();
        boolean isAppendStatusLabel = PrometheusConfiguration.get().isAppendStatusLabel();
        String[] buildParameterNamesAsArray = PrometheusConfiguration.get().getLabeledBuildParameterNamesAsArray();

        Result runResult = run.getResult();
        String[] labelValueArray = getBaseLabelValues(job);
        if (isAppendParamLabel) {
            String params = Runs.getBuildParameters(run).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(";"));
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
        return labelValueArray;
    }
}
