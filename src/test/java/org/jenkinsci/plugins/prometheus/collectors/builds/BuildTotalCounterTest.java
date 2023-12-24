package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class BuildTotalCounterTest extends MockedRunCollectorTest {

    @Test
    public void testIncreasedOnUnstableBuild() {
        when(mock.getResult()).thenReturn(Result.UNSTABLE);
        testSingleCalculation(2);
    }

    @Test
    public void tesIncreasedOnSuccessfulBuild() {
        when(mock.getResult()).thenReturn(Result.SUCCESS);
        testSingleCalculation(2);
    }

    @Test
    public void testNothingIsIncreasedOnNotBuiltBuild() {
        when(mock.getResult()).thenReturn(Result.NOT_BUILT);
        testSingleCalculation(0);
    }

    @Test
    public void testIncreasedOnAbortedBuild() {
        when(mock.getResult()).thenReturn(Result.ABORTED);
        testSingleCalculation(2);
    }

    @Test
    public void testIncreasedOnBuildResultFailure() {
        when(mock.getResult()).thenReturn(Result.FAILURE);
        testSingleCalculation(2);
    }
   
    private void testSingleCalculation(int expectedCount) {
        BuildTotalCounter sut = new BuildTotalCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(expectedCount, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_failed_build_count_total")) {
                Assertions.assertEquals(1.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_failed_build_count_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }
}