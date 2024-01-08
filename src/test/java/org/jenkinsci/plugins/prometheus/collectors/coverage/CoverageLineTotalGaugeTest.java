package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Metric;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.prometheus.client.Collector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoverageLineTotalGaugeTest extends CoverageTest {


    public CoverageLineTotalGaugeTest() {
        super(Baseline.PROJECT, Metric.LINE);
    }

    @Test
    public void testMissed() {
        setUpSuccessfulMocksForTotal();
        CoverageLineTotalGauge sut = new CoverageLineTotalGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of lines total", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_line_total", familySamples.name);

        List<Collector.MetricFamilySamples.Sample> samples = familySamples.samples;

        Assertions.assertEquals(1, samples.size());

        Collector.MetricFamilySamples.Sample sample = samples.get(0);
        Assertions.assertEquals(15.0, sample.value);
        Assertions.assertEquals("myJob", sample.labelValues.get(0));

    }

    @Test
    public void testNothingFailsIfNoCoverageFound() {
        setUpUnsuccessfulMocks();

        CoverageLineTotalGauge sut = new CoverageLineTotalGauge(new String[]{"job"}, getNamespace(), getSubSystem());

        sut.calculateMetric(mock, new String[]{"myJob"});

        List<Collector.MetricFamilySamples> metricFamilySamples = sut.collect();
        Assertions.assertEquals(1, metricFamilySamples.size());

        Collector.MetricFamilySamples familySamples = metricFamilySamples.get(0);

        Assertions.assertEquals("Returns the number of lines total", familySamples.help);
        Assertions.assertEquals("default_jenkins_builds_coverage_line_total", familySamples.name);

        List<Collector.MetricFamilySamples.Sample> samples = familySamples.samples;

        Assertions.assertEquals(1, samples.size());

        Collector.MetricFamilySamples.Sample sample = samples.get(0);
        Assertions.assertEquals(-1, sample.value);
        Assertions.assertEquals("myJob", sample.labelValues.get(0));
    }
}