package org.jenkinsci.plugins.prometheus.util;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Runs {

    public static boolean includeBuildInMetrics(Run build) {
        boolean include = false;
        if (!build.isBuilding()) {
            include = true;
            Result result = build.getResult();
            if (result != null) {
                if (result == Result.ABORTED) {
                    include = PrometheusConfiguration.get().isCountAbortedBuilds();
                } else if (result == Result.FAILURE) {
                    include = PrometheusConfiguration.get().isCountFailedBuilds();
                } else if (result == Result.NOT_BUILT) {
                    include = PrometheusConfiguration.get().isCountNotBuiltBuilds();
                } else if (result == Result.SUCCESS) {
                    include = PrometheusConfiguration.get().isCountSuccessfulBuilds();
                } else if (result == Result.UNSTABLE) {
                    include = PrometheusConfiguration.get().isCountUnstableBuilds();
                }
            }
        }
        return include;
    }

    /**
     * Checks if a run should be included in per-build metrics based on retention settings.
     * This method checks both time-based (max age) and count-based (max builds) limits.
     *
     * @param run the run to check
     * @param buildIndex the 0-based index of this build (0 = latest build)
     * @return true if the run should be included in per-build metrics, false otherwise
     */
    public static boolean includeRunInPerBuildMetrics(Run<?, ?> run, int buildIndex) {
        PrometheusConfiguration config = PrometheusConfiguration.get();
        
        // Check count-based limit
        int maxBuilds = config.getPerBuildMetricsMaxBuilds();
        if (maxBuilds > 0 && buildIndex >= maxBuilds) {
            return false;
        }
        
        // Check time-based limit (based on build end time)
        long maxAgeInHours = config.getPerBuildMetricsMaxAgeInHours();
        if (maxAgeInHours > 0) {
            long maxAgeInMillis = TimeUnit.HOURS.toMillis(maxAgeInHours);
            // Calculate build end time: start time + duration
            long buildEndTime = run.getTimeInMillis() + run.getDuration();
            long now = System.currentTimeMillis();
            if ((now - buildEndTime) > maxAgeInMillis) {
                return false;
            }
        }
        
        return true;
    }

    public static Map<String, Object> getBuildParameters(Run build) {
        List<ParametersAction> actions = build.getActions(ParametersAction.class);
        Map<String, Object> answer = new HashMap<>();
        for (ParametersAction action : actions) {
            List<ParameterValue> parameters = action.getParameters();
            if (parameters != null) {
                for (ParameterValue parameter : parameters) {
                    String name = parameter.getName();
                    Object value = parameter.getValue();
                    answer.put(name, value);
                }
            }
        }
        return answer;
    }
}
