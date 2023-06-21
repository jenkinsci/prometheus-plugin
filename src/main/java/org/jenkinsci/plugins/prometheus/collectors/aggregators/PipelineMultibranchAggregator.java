package org.jenkinsci.plugins.prometheus.collectors.aggregators;

import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

public class PipelineMultibranchAggregator implements MetricAggregator {
    @Override
    public boolean isHandleJenkinsObject(Object jenkinsObject) {
        return jenkinsObject instanceof Job || jenkinsObject instanceof Job;
    }

    @Override
    public String[] getLabelValues(Object jenkinsObject, String[] aggregatedLabelValues) {
        if (jenkinsObject instanceof Run){
            aggregatedLabelValues[0] = ((Run)jenkinsObject).getParent().getClass().getName();
        } else if (jenkinsObject instanceof Job) {
            aggregatedLabelValues[0] = getLabelJobName((Job)jenkinsObject);
        }
        return aggregatedLabelValues;
    }

    private String getLabelJobName(Job job) {
        if (job.getParent() != null && job.getParent().getClass().getName() == "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject") {
            return job.getParent().getFullName();
        }
        return job.getFullName();
    }

}
