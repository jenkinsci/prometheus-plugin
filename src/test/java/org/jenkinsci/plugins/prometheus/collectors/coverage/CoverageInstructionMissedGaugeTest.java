package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Metric;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.prometheus.client.Collector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CoverageInstructionMissedGaugeTest extends CoverageTest {


    public CoverageInstructionMissedGaugeTest() {
        super(Baseline.PROJECT, Metric.INSTRUCTION);
    }

    @Test
    public void testCovered() {
        setUpSuccessfulMocksForMissed();
        CoverageInstructionMissedGauge sut = new CoverageInstructionMissedGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of instructions missed", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_instruction_missed", familySamples.name);

        List<Collector.MetricFamilySamples.Sample> samples = familySamples.samples;

        Assertions.assertEquals(1, samples.size());

        Collector.MetricFamilySamples.Sample sample = samples.get(0);
        Assertions.assertEquals(5.0, sample.value);
        Assertions.assertEquals("myJob", sample.labelValues.get(0));

    }

    @Test
    public void testNothingFailsIfNoCoverageFound() {
        setUpUnsuccessfulMocks();

        CoverageInstructionMissedGauge sut = new CoverageInstructionMissedGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of instructions missed", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_instruction_missed", familySamples.name);

        Assertions.assertEquals(0, familySamples.samples.size());
    }

}