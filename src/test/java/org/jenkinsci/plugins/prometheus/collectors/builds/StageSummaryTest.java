package org.jenkinsci.plugins.prometheus.collectors.builds;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;


public class StageSummaryTest extends MockedRunCollectorTest {

    @Test
    public void testNothingCalculatedWhenJobIsBuilding() {
        Mockito.when(mock.isBuilding()).thenReturn(true);

        StageSummary sut = new StageSummary(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenJobNotAWorkflowJob() {
        Mockito.when(mock.isBuilding()).thenReturn(false);


        StageSummary sut = new StageSummary(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenThereIsNoWorkflowExecution() {

        StageSummary sut = new StageSummary(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());
    }

    // TODO: Write more tests

}