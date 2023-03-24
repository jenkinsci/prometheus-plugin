package org.jenkinsci.plugins.prometheus;

import hudson.model.Api;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.coverage.model.Coverage;
import io.jenkins.plugins.coverage.model.CoverageBuildAction;
import io.jenkins.plugins.coverage.model.CoverageMetric;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeCoverageCollector extends BaseCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeCoverageCollector.class);

    @Override
    public List<MetricFamilySamples> collect() {

        if (!isCodeCoverageAPIPluginLoaded()) {
            LOGGER.warn("Cannot collect code coverage data because plugin Code Coverage API (shortname: 'code-coverage-api') is not loaded.");
            return Collections.emptyList();
        }

        if (!isCodeCoverageCollectionConfigured()) {
            return Collections.emptyList();
        }

        List<MetricFamilySamples> samples = new ArrayList<>();
        Jobs.forEachJob(job -> CollectionUtils.addIgnoreNull(samples, collectCoverageMetricForJob(job)));
        return samples;
    }

    private List<MetricFamilySamples> collectCoverageMetricForJob(Job job) {
        if (job == null) {
            return Collections.emptyList();
        }

        Run lastBuild = job.getLastBuild();
        if (lastBuild == null || lastBuild.isBuilding()) {
            return Collections.emptyList();
        }

        CoverageBuildAction coverageBuildAction = lastBuild.getAction(CoverageBuildAction.class);
        if (coverageBuildAction == null) {
            return Collections.emptyList();
        }

        List<MetricFamilySamples> samples = new ArrayList<>();

        Coverage classCoverage = coverageBuildAction.getCoverage(CoverageMetric.CLASS);
        addGauge(samples, "class_covered", classCoverage.getCovered(), new String[]{"code_coverage"}, "Returns the number of classes covered");
        addGauge(samples, "class_missed", classCoverage.getMissed(), new String[]{"code_coverage"}, "Returns the number of classes missed");
        addGauge(samples, "class_total", classCoverage.getTotal(), new String[]{"code_coverage"}, "Returns the number of classes total");

        Coverage branchCoverage = coverageBuildAction.getCoverage(CoverageMetric.BRANCH);
        addGauge(samples, "branch_covered", branchCoverage.getCovered(), new String[]{"code_coverage"}, "Returns the number of branches covered");
        addGauge(samples, "branch_missed", branchCoverage.getMissed(), new String[]{"code_coverage"}, "Returns the number of branches missed");
        addGauge(samples, "branch_total", branchCoverage.getTotal(), new String[]{"code_coverage"}, "Returns the number of branches total");

        Coverage instructionCoverage = coverageBuildAction.getCoverage(CoverageMetric.INSTRUCTION);
        addGauge(samples, "instruction_covered", instructionCoverage.getCovered(), new String[]{"code_coverage"}, "Returns the number of instructions total");
        addGauge(samples, "instruction_missed", instructionCoverage.getMissed(), new String[]{"code_coverage"}, "Returns the number of instructions total");
        addGauge(samples, "instruction_total", instructionCoverage.getTotal(), new String[]{"code_coverage"}, "Returns the number of instructions total");

        Coverage fileCoverage = coverageBuildAction.getCoverage(CoverageMetric.FILE);
        addGauge(samples, "file_covered", fileCoverage.getCovered(), new String[]{"code_coverage"}, "Returns the number of files covered");
        addGauge(samples, "file_missed", fileCoverage.getMissed(), new String[]{"code_coverage"}, "Returns the number of files missed");
        addGauge(samples, "file_total", fileCoverage.getTotal(), new String[]{"code_coverage"}, "Returns the number of files total");

        return samples;
    }

    private void addGauge(List<MetricFamilySamples> samples, String name, double value, String[] labels, String helptext) {
        Gauge gauge = newGaugeBuilder(labels).name(name).help(helptext).create();
        gauge.set(value);
        samples.addAll(gauge.collect());
    }

    private boolean isCodeCoverageAPIPluginLoaded() {
        return Jenkins.get().getPlugin("code-coverage-api") != null;
    }

    private boolean isCodeCoverageCollectionConfigured() {
        return PrometheusConfiguration.get().isCollectCodeCoverage();
    }
}
