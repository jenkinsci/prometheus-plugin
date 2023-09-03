package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Metric;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.prometheus.client.Collector;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class CoverageBranchMissedGaugeTest extends CoverageTest {



    public CoverageBranchMissedGaugeTest() {
        super(Baseline.PROJECT, Metric.BRANCH);
    }

    @Test
    public void testMissed() {
        setUpSuccessfulMocksForMissed();
        CoverageBranchMissedGauge sut = new CoverageBranchMissedGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of branches missed", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_branch_missed", familySamples.name);

        List<Collector.MetricFamilySamples.Sample> samples = familySamples.samples;

        Assertions.assertEquals(1, samples.size());

        Collector.MetricFamilySamples.Sample sample = samples.get(0);
        Assertions.assertEquals(5.0, sample.value);
        Assertions.assertEquals("myJob", sample.labelValues.get(0));

    }

    @Test
    public void testNothingFailsIfNoCoverageFound() {
        setUpUnsuccessfulMocks();

        CoverageBranchMissedGauge sut = new CoverageBranchMissedGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of branches missed", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_branch_missed", familySamples.name);

        Assertions.assertEquals(0, familySamples.samples.size());
    }

}