package org.jenkinsci.plugins.prometheus.collectors.coverage;

import hudson.model.Run;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseCollectorFactory;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.MetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.NoOpMetricCollector;

public class CoverageCollectorFactory extends BaseCollectorFactory {

    public MetricCollector<Run<?, ?>, ? extends Collector> createCollector(CollectorType type, String[] labelNames) {
        switch (type) {
            case COVERAGE_CLASS_COVERED:
                return saveBuildCollector(new CoverageClassCoveredGauge(labelNames, namespace, subsystem));
            case COVERAGE_CLASS_MISSED:
                return saveBuildCollector(new CoverageClassMissedGauge(labelNames, namespace, subsystem));
            case COVERAGE_CLASS_TOTAL:
                return saveBuildCollector(new CoverageClassTotalGauge(labelNames, namespace, subsystem));
            case COVERAGE_CLASS_PERCENT:
                return saveBuildCollector(new CoverageClassPercentGauge(labelNames, namespace, subsystem));

            case COVERAGE_BRANCH_COVERED:
                return saveBuildCollector(new CoverageBranchCoveredGauge(labelNames, namespace, subsystem));
            case COVERAGE_BRANCH_MISSED:
                return saveBuildCollector(new CoverageBranchMissedGauge(labelNames, namespace, subsystem));
            case COVERAGE_BRANCH_TOTAL:
                return saveBuildCollector(new CoverageBranchTotalGauge(labelNames, namespace, subsystem));
            case COVERAGE_BRANCH_PERCENT:
                return saveBuildCollector(new CoverageBranchPercentGauge(labelNames, namespace, subsystem));

            case COVERAGE_INSTRUCTION_COVERED:
                return saveBuildCollector(new CoverageInstructionCoveredGauge(labelNames, namespace, subsystem));
            case COVERAGE_INSTRUCTION_MISSED:
                return saveBuildCollector(new CoverageInstructionMissedGauge(labelNames, namespace, subsystem));
            case COVERAGE_INSTRUCTION_TOTAL:
                return saveBuildCollector(new CoverageInstructionTotalGauge(labelNames, namespace, subsystem));
            case COVERAGE_INSTRUCTION_PERCENT:
                return saveBuildCollector(new CoverageInstructionPercentGauge(labelNames, namespace, subsystem));

            case COVERAGE_FILE_COVERED:
                return saveBuildCollector(new CoverageFileCoveredGauge(labelNames, namespace, subsystem));
            case COVERAGE_FILE_MISSED:
                return saveBuildCollector(new CoverageFileMissedGauge(labelNames, namespace, subsystem));
            case COVERAGE_FILE_TOTAL:
                return saveBuildCollector(new CoverageFileTotalGauge(labelNames, namespace, subsystem));
            case COVERAGE_FILE_PERCENT:
                return saveBuildCollector(new CoverageFilePercentGauge(labelNames, namespace, subsystem));

            case COVERAGE_LINE_COVERED:
                return saveBuildCollector(new CoverageLineCoveredGauge(labelNames, namespace, subsystem));
            case COVERAGE_LINE_MISSED:
                return saveBuildCollector(new CoverageLineMissedGauge(labelNames, namespace, subsystem));
            case COVERAGE_LINE_TOTAL:
                return saveBuildCollector(new CoverageLineTotalGauge(labelNames, namespace, subsystem));
            case COVERAGE_LINE_PERCENT:
                return saveBuildCollector(new CoverageLinePercentGauge(labelNames, namespace, subsystem));

            default:
                return new NoOpMetricCollector<>();
        }
    }
}
