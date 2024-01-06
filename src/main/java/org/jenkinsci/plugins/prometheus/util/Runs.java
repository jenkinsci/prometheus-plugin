package org.jenkinsci.plugins.prometheus.util;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
