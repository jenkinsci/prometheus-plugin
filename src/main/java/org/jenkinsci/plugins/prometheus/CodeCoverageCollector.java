package org.jenkinsci.plugins.prometheus;

import hudson.model.Run;
import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.CollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.config.disabledmetrics.MetricStatusChecker;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CodeCoverageCollector extends Collector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeCoverageCollector.class);

    @Override
    public List<MetricFamilySamples> collect() {

        if (!isCoveragePluginLoaded()) {
            LOGGER.debug("Cannot collect code coverage data because plugin Code Coverage API (shortname: 'code-coverage-api') is not loaded.");
            return Collections.emptyList();
        }

        if (!isCoverageCollectionConfigured()) {
            return Collections.emptyList();
        }

        List<MetricCollector<Run<?,?>, ? extends Collector>> collectors = createCollectors();

        return collectCoverageMetricForJob(collectors);
    }

    private List<MetricCollector<Run<?, ?>, ? extends Collector>> createCollectors() {
        CollectorFactory factory = new CollectorFactory();
        List<MetricCollector<Run<?,?>, ? extends Collector>> collectors = new ArrayList<>();

        String jobAttributeName = PrometheusConfiguration.get().getJobAttributeName();

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_COVERED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_MISSED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_TOTAL, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_CLASS_PERCENT, new String[]{jobAttributeName}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_COVERED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_MISSED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_TOTAL, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_BRANCH_PERCENT, new String[]{jobAttributeName}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_COVERED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_MISSED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_TOTAL, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_INSTRUCTION_PERCENT, new String[]{jobAttributeName}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_COVERED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_MISSED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_TOTAL, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_FILE_PERCENT, new String[]{jobAttributeName}));

        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_LINE_COVERED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_LINE_MISSED, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_LINE_TOTAL, new String[]{jobAttributeName}));
        collectors.add(factory.createCoverageRunCollector(CollectorType.COVERAGE_LINE_PERCENT, new String[]{jobAttributeName}));

        return collectors;
    }

    private List<MetricFamilySamples> collectCoverageMetricForJob(List<MetricCollector<Run<?, ?>, ? extends Collector>> collectors) {

        Jobs.forEachJob(job -> {
            Run<?,?> lastBuild = job.getLastBuild();
            if (lastBuild == null || lastBuild.isBuilding()) {
                return;
            }
            if (!MetricStatusChecker.isJobEnabled(job.getFullName())) {
                LOGGER.debug("Job '{}' is excluded by configuration", job.getFullName());
                return;
            }

            CoverageBuildAction coverageBuildAction = lastBuild.getAction(CoverageBuildAction.class);
            if (coverageBuildAction == null) {
                return;
            }

            collectors.forEach(c -> c.calculateMetric(lastBuild, new String[]{job.getFullName()}));
        });

        return collectors.stream()
                .map(MetricCollector::collect)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private boolean isCoveragePluginLoaded() {
        return Jenkins.get().getPlugin("coverage") != null;
    }

    private boolean isCoverageCollectionConfigured() {
        return PrometheusConfiguration.get().isCollectCodeCoverage();
    }
}
