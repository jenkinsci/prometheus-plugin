package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Result;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildFailedCounter;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

import static org.mockito.Mockito.when;

public class BuildFailedCounterTest extends MockedRunCollectorTest {


    @Test
    public void testNothingIsIncreasedOnUnstableBuild() {
        when(mock.getResult()).thenReturn(Result.UNSTABLE);
        testNonFailureStateBuild();
    }

    @Test
    public void testNothingIsIncreasedOnSuccessfulBuild() {
        when(mock.getResult()).thenReturn(Result.SUCCESS);
        testNonFailureStateBuild();
    }

    @Test
    public void testNothingIsIncreasedOnNotBuiltBuild() {
        when(mock.getResult()).thenReturn(Result.NOT_BUILT);
        testSingleCalculation();
    }

    @Test
    public void testNothingIsIncreasedOnAbortedBuild() {
        when(mock.getResult()).thenReturn(Result.ABORTED);
        testSingleCalculation();
    }

    @Test
    public void testCollectOnBuildResultFailure() {
        when(mock.getResult()).thenReturn(Result.FAILURE);
        testSingleCalculation();
    }

    private void testSingleCalculation() {
        BuildFailedCounter sut = new BuildFailedCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(2, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_failed_build_count_total")) {
                Assertions.assertEquals(1.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_failed_build_count_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }

    @Test
    public void testCounterIsIncreasedOnBuildResultFailure() {
        when(mock.getResult()).thenReturn(Result.FAILURE);

        BuildFailedCounter sut = new BuildFailedCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(2, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_failed_build_count_total")) {
                Assertions.assertEquals(2.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_failed_build_count_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }

    private void testNonFailureStateBuild() {
        BuildFailedCounter sut = new BuildFailedCounter(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size(), "Would expect one result");
    }
}