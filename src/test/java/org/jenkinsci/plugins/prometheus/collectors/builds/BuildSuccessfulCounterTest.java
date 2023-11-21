package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class BuildSuccessfulCounterTest extends MockedRunCollectorTest {

    @Test
    public void testNothingIsIncreasedOnUnstableBuild() {
        when(mock.getResult()).thenReturn(Result.UNSTABLE);
        testSingleCalculation();
    }

    @Test
    public void testNothingIsIncreasedOnSuccessfulBuild() {
        when(mock.getResult()).thenReturn(Result.SUCCESS);
        testSingleCalculation();
    }

    @Test
    public void testNothingIsIncreasedOnNotBuiltBuild() {
        when(mock.getResult()).thenReturn(Result.NOT_BUILT);
        testNonSuccessStateBuild();
    }

    @Test
    public void testNothingIsIncreasedOnAbortedBuild() {
        when(mock.getResult()).thenReturn(Result.ABORTED);
        testNonSuccessStateBuild();
    }

    @Test
    public void testCollectOnBuildResultFailure() {
        when(mock.getResult()).thenReturn(Result.FAILURE);
        testNonSuccessStateBuild();
    }

    private void testSingleCalculation() {
        BuildSuccessfulCounter sut = new BuildSuccessfulCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());

        Assertions.assertEquals(2, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_success_build_count_total")) {
                Assertions.assertEquals(1.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_success_build_count_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }

    @Test
    public void testCounterIsIncreasedOnBuildResultFailure() {
        when(mock.getResult()).thenReturn(Result.SUCCESS);

        BuildSuccessfulCounter sut = new BuildSuccessfulCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());

        System.out.println(collect.get(0).samples);
        Assertions.assertEquals(2, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_success_build_count_total")) {
                Assertions.assertEquals(2.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_success_build_count_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }

    private void testNonSuccessStateBuild() {
        BuildSuccessfulCounter sut = new BuildSuccessfulCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size(), "Would expect one result");
    }
}