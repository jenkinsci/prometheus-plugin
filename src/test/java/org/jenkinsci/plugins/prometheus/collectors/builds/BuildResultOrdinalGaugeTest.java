package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class BuildResultOrdinalGaugeTest extends MockedRunCollectorTest {


    @Test
    public void testNothingCalculatedAsRunNotYetOver() {

        Mockito.when(mock.getResult()).thenReturn(null);

        BuildResultOrdinalGauge sut = new BuildResultOrdinalGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size(), "Would expect no result");
    }

    @Test
    public void testOrdinalCalculated() {

        Mockito.when(mock.getResult()).thenReturn(Result.SUCCESS);

        BuildResultOrdinalGauge sut = new BuildResultOrdinalGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size(), "Would expect one result");


        Assertions.assertEquals("default_jenkins_builds_build_result_ordinal", collect.get(0).samples.get(0).name);
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);

    }

    @Test
    public void testPrefixedOrdinalCalculated() {

        Mockito.when(mock.getResult()).thenReturn(Result.SUCCESS);

        BuildResultOrdinalGauge sut = new BuildResultOrdinalGauge(getLabelNames(), getNamespace(), getSubSystem(), "last");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size(), "Would expect one result");


        Assertions.assertEquals("default_jenkins_builds_last_build_result_ordinal", collect.get(0).samples.get(0).name);
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);

    }
}