package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        List<List<MetricFamilySamples>> samples = new ArrayList<>();
        Jobs.forEachJob(job -> CollectionUtils.addIgnoreNull(samples, collectCoverageMetricForJob(job)));


        return samples.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<MetricFamilySamples> collectCoverageMetricForJob(Job<?,?> job) {
        if (job == null) {
            return Collections.emptyList();
        }

        Run<?,?> lastBuild = job.getLastBuild();
        if (lastBuild == null || lastBuild.isBuilding()) {
            return Collections.emptyList();
        }

        CoverageBuildAction coverageBuildAction = lastBuild.getAction(CoverageBuildAction.class);
        if (coverageBuildAction == null) {
            return Collections.emptyList();
        }

        CollectorFactory factory = new CollectorFactory();
        List<MetricCollector<Run<?,?>, ? extends Collector>> collectors = new ArrayList<>();

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_COVERED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_MISSED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_TOTAL, new String[]{"job_name"}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_COVERED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_MISSED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_TOTAL, new String[]{"job_name"}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_COVERED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_MISSED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_TOTAL, new String[]{"job_name"}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_COVERED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_MISSED, new String[]{"job_name"}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_TOTAL, new String[]{"job_name"}));

        collectors.forEach(c -> c.calculateMetric(lastBuild, new String[]{job.getName()}));

        return collectors.stream()
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    private boolean isCodeCoverageAPIPluginLoaded() {
        return Jenkins.get().getPlugin("code-coverage-api") != null;
    }

    private boolean isCodeCoverageCollectionConfigured() {
        return PrometheusConfiguration.get().isCollectCodeCoverage();
    }
}
