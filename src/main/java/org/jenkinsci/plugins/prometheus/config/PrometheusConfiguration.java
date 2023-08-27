package org.jenkinsci.plugins.prometheus.config;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.YesNoMaybe;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.config.disabledmetrics.DisabledMetricConfig;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Müller
 */
@Extension(dynamicLoadable = YesNoMaybe.NO)
public class PrometheusConfiguration extends GlobalConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusConfiguration.class);

    private static final String PROMETHEUS_ENDPOINT = "PROMETHEUS_ENDPOINT";
    private static final String DEFAULT_ENDPOINT = "prometheus";
    static final String COLLECTING_METRICS_PERIOD_IN_SECONDS = "COLLECTING_METRICS_PERIOD_IN_SECONDS";
    static final long DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS = TimeUnit.MINUTES.toSeconds(2);
    static final String COLLECT_DISK_USAGE = "COLLECT_DISK_USAGE";

    private String urlName = null;
    private String additionalPath;
    private String defaultNamespace = "default";
    private String jobAttributeName = "jenkins_job";
    private boolean useAuthenticatedEndpoint;
    private long collectingMetricsPeriodInSeconds = -1L;

    private boolean countSuccessfulBuilds = true;
    private boolean countUnstableBuilds = true;
    private boolean countFailedBuilds = true;
    private boolean countNotBuiltBuilds = true;
    private boolean countAbortedBuilds = true;
    private boolean fetchTestResults = true;

    private boolean processingDisabledBuilds = false;

    private boolean appendParamLabel = false;
    private boolean appendStatusLabel = false;
    private boolean perBuildMetrics = false;

    private boolean environmentVariableSet;

    private String labeledBuildParameterNames = "";

    private boolean collectDiskUsage = true;
    private boolean collectCodeCoverage = false;
    private boolean collectNodeStatus = true;

    private DisabledMetricConfig disabledMetricConfig = new DisabledMetricConfig(new ArrayList<>());


    public PrometheusConfiguration() {
        load();
        setPath(getPath());
        setCollectingMetricsPeriodInSeconds(collectingMetricsPeriodInSeconds);
        setCollectDiskUsageBasedOnEnvironmentVariableIfDefined();
        environmentVariableSet = isValidBooleanEnv(COLLECT_DISK_USAGE);
    }

    public static PrometheusConfiguration get() {
        return (PrometheusConfiguration) Jenkins.get().getDescriptor(PrometheusConfiguration.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public String getPath() {
        return StringUtils.isEmpty(additionalPath) ? urlName : urlName + additionalPath;
    }

    @DataBoundSetter
    public void setPath(String path) {
        if (path == null) {
            Map<String, String> env = System.getenv();
            path = env.getOrDefault(PROMETHEUS_ENDPOINT, DEFAULT_ENDPOINT);
        }
        urlName = path.split("/")[0];
        List<String> pathParts = Arrays.asList(path.split("/"));
        additionalPath = (pathParts.size() > 1 ? "/" : "") + StringUtils.join(pathParts.subList(1, pathParts.size()), "/");
    }

    public String getJobAttributeName() {
        return jobAttributeName;
    }

    @DataBoundSetter
    public void setJobAttributeName(String jobAttributeName) {
        this.jobAttributeName = jobAttributeName;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    @DataBoundSetter
    public void setDefaultNamespace(String path) {
        this.defaultNamespace = path;
    }

    @DataBoundSetter
    public void setCollectDiskUsage(Boolean collectDiskUsage) {
        this.collectDiskUsage = collectDiskUsage;
    }

    public void setCollectDiskUsageBasedOnEnvironmentVariableIfDefined() {
        try {
            this.collectDiskUsage = getBooleanEnvironmentVariableOrThrowException(COLLECT_DISK_USAGE);
        } catch (IllegalArgumentException e) {
            logger.warn("Unable to parse environment variable '{}'. Must either be 'true' or 'false'. Ignoring...", COLLECT_DISK_USAGE);
        }
    }

    private boolean getBooleanEnvironmentVariableOrThrowException(String key) throws IllegalArgumentException {
        if (isValidBooleanEnv(key)) {
            return Boolean.parseBoolean(System.getenv(key));
        }
        throw new IllegalArgumentException();
    }

    private boolean isValidBooleanEnv(String key) {
        String value = System.getenv(key);
        if (value != null) {
            return ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
        }
        return false;
    }

    public boolean getCollectDiskUsage() {
        return collectDiskUsage;
    }

    public long getCollectingMetricsPeriodInSeconds() {
        return collectingMetricsPeriodInSeconds;
    }

    @DataBoundSetter
    public void setCollectingMetricsPeriodInSeconds(long collectingMetricsPeriodInSeconds) {
        if (collectingMetricsPeriodInSeconds == -1L) {
            this.collectingMetricsPeriodInSeconds = parseLongFromEnv();
        } else {
            this.collectingMetricsPeriodInSeconds = collectingMetricsPeriodInSeconds;
        }
    }

    public boolean isUseAuthenticatedEndpoint() {
        return useAuthenticatedEndpoint;
    }

    @DataBoundSetter
    public void setUseAuthenticatedEndpoint(boolean useAuthenticatedEndpoint) {
        this.useAuthenticatedEndpoint = useAuthenticatedEndpoint;
    }

    public boolean isCountSuccessfulBuilds() {
        return countSuccessfulBuilds;
    }

    @DataBoundSetter
    public void setCountSuccessfulBuilds(boolean countSuccessfulBuilds) {
        this.countSuccessfulBuilds = countSuccessfulBuilds;
    }

    public boolean isCountUnstableBuilds() {
        return countUnstableBuilds;
    }

    @DataBoundSetter
    public void setCountUnstableBuilds(boolean countUnstableBuilds) {
        this.countUnstableBuilds = countUnstableBuilds;
    }

    public boolean isCountFailedBuilds() {
        return countFailedBuilds;
    }

    @DataBoundSetter
    public void setCountFailedBuilds(boolean countFailedBuilds) {
        this.countFailedBuilds = countFailedBuilds;
    }

    public boolean isCountNotBuiltBuilds() {
        return countNotBuiltBuilds;
    }

    @DataBoundSetter
    public void setCountNotBuiltBuilds(boolean countNotBuiltBuilds) {
        this.countNotBuiltBuilds = countNotBuiltBuilds;
    }

    public boolean isCountAbortedBuilds() {
        return countAbortedBuilds;
    }

    @DataBoundSetter
    public void setCountAbortedBuilds(boolean countAbortedBuilds) {
        this.countAbortedBuilds = countAbortedBuilds;
    }

    public boolean isFetchTestResults() {
        return fetchTestResults;
    }

    @DataBoundSetter
    public void setFetchTestResults(boolean fetchTestResults) {
        this.fetchTestResults = fetchTestResults;
    }

    public boolean isProcessingDisabledBuilds() {
        return processingDisabledBuilds;
    }

    @DataBoundSetter
    public void setProcessingDisabledBuilds(boolean processingDisabledBuilds) {
        this.processingDisabledBuilds = processingDisabledBuilds;
    }

    public boolean isAppendParamLabel() {
        return appendParamLabel;
    }

    @DataBoundSetter
    public void setAppendParamLabel(boolean appendParamLabel) {
        this.appendParamLabel = appendParamLabel;
    }

    public boolean isAppendStatusLabel() {
        return appendStatusLabel;
    }

    @DataBoundSetter
    public void setAppendStatusLabel(boolean appendStatusLabel) {
        this.appendStatusLabel = appendStatusLabel;
    }

    public boolean isPerBuildMetrics() {
        return perBuildMetrics;
    }

    @DataBoundSetter
    public void setPerBuildMetrics(boolean perBuildMetrics) {
        this.perBuildMetrics = perBuildMetrics;
    }

    public boolean isCollectNodeStatus() {
        return collectNodeStatus;
    }

    @DataBoundSetter
    public void setCollectNodeStatus(boolean collectNodeStatus) {
        this.collectNodeStatus = collectNodeStatus;
    }

    public String getUrlName() {
        return urlName;
    }

    public String getAdditionalPath() {
        return additionalPath;
    }

    public String getLabeledBuildParameterNames() {
        return labeledBuildParameterNames;
    }

    @DataBoundSetter
    public void setLabeledBuildParameterNames(String labeledBuildParameterNames) {
        this.labeledBuildParameterNames = labeledBuildParameterNames;
    }

    public String[] getLabeledBuildParameterNamesAsArray() {
        return parseParameterNamesFromStringSeparatedByComma(labeledBuildParameterNames);
    }

    public DisabledMetricConfig getDisabledMetricConfig() {
        return disabledMetricConfig;
    }

    @DataBoundSetter
    public void setDisabledMetricConfig(DisabledMetricConfig disabledMetricConfig) {
        this.disabledMetricConfig = disabledMetricConfig;
    }

    public boolean isCollectCodeCoverage() {
        return collectCodeCoverage;
    }

    public boolean isCodeCoverageApiPluginInstalled() {
        return Jenkins.get().getPlugin("code-coverage-api") != null;
    }

    public void setCollectCodeCoverage(boolean collectCodeCoverage) {
        this.collectCodeCoverage = collectCodeCoverage;
    }

    public FormValidation doCheckPath(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.error(Messages.path_required());
        } else if (System.getenv().containsKey(PROMETHEUS_ENDPOINT)) {
            return FormValidation.warning(Messages.path_environment_override(PROMETHEUS_ENDPOINT, System.getenv(PROMETHEUS_ENDPOINT)));
        } else {
            return FormValidation.ok();
        }
    }

    public FormValidation doCheckCollectingMetricsPeriodInSeconds(@QueryParameter String value) {
        try {
            long longValue = Long.parseLong(value);
            if (longValue > 0) {
                return FormValidation.ok();
            }
        } catch (NumberFormatException ignore) {
            // ignore exception. If it comes it's not a positive long
        }
        return FormValidation.error("CollectingMetricsPeriodInSeconds must be a positive value");
    }

    private long parseLongFromEnv() {
        Map<String, String> env = System.getenv();
        String message = String.format("COLLECTING_METRICS_PERIOD_IN_SECONDS must be a positive integer. The default value: '%d' will be used instead of provided.", DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS);
        try {
            return Optional.ofNullable(env.get(COLLECTING_METRICS_PERIOD_IN_SECONDS))
                    .map(Long::parseLong)
                    .filter(v -> v > 0)
                    .orElseGet(() -> {
                        logger.warn(message);
                        return DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
                    });
        } catch (NumberFormatException e) {
            logger.warn(message);
            return DEFAULT_COLLECTING_METRICS_PERIOD_IN_SECONDS;
        }
    }

    private String[] parseParameterNamesFromStringSeparatedByComma(String stringValue) {
        if (stringValue == null || stringValue.trim().length() < 1) {
            return new String[]{};
        }
        return stringValue.split("\\s*,\\s*");
    }

    public boolean isEnvironmentVariableSet() {
        return environmentVariableSet;
    }
}
