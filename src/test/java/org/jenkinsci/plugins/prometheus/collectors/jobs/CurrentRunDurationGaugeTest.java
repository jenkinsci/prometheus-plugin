package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Run;
import io.prometheus.client.Collector;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrentRunDurationGaugeTest extends JobCollectorTest {

    @Mock
    Run currentRun;

    @Override
    @Test
    public void testCollectResult() {
        when(currentRun.isBuilding()).thenReturn(true);
        when(currentRun.getStartTimeInMillis()).thenReturn(1000L);
        when(job.getLastBuild()).thenReturn(currentRun);

        CurrentRunDurationGauge sut = new CurrentRunDurationGauge(getLabelNames(), getNamespace(), getSubSystem());

        try (MockedStatic<Clock> mock = Mockito.mockStatic(Clock.class)) {

            Clock mockedClock = mock(Clock.class);
            mock.when(Clock::systemUTC).thenReturn(mockedClock);
            when(mockedClock.millis()).thenReturn(2000L);

            sut.calculateMetric(job, new String[]{"job1", "NA"});
            List<Collector.MetricFamilySamples> collect = sut.collect();

            validateMetricFamilySampleListSize(collect, 1);

            Collector.MetricFamilySamples samples = collect.get(0);
            validateNames(samples, new String[]{"default_jenkins_builds_running_build_duration_milliseconds"});
            validateMetricFamilySampleSize(samples, 1);
            validateValue(samples.samples.get(0), 1000.0);
        }
    }

    @Test
    public void testCurrentlyRunningLabelValue() {
        when(currentRun.isBuilding()).thenReturn(true);
        when(currentRun.getStartTimeInMillis()).thenReturn(1000L);
        when(job.getLastBuild()).thenReturn(currentRun);

        CurrentRunDurationGauge sut = new CurrentRunDurationGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});

        List<String> labelNames = sut.collect().get(0).samples.get(0).labelNames;
        List<String> labelValues = sut.collect().get(0).samples.get(0).labelValues;

        Assertions.assertEquals(2, labelNames.size());
        Assertions.assertEquals(2, labelValues.size());

        double value = sut.collect().get(0).samples.get(0).value;

        Assertions.assertNotEquals(0.0, value);
    }

    @Test
    public void testCurrentlyNotRunning() {
        when(currentRun.isBuilding()).thenReturn(false);
        when(job.getLastBuild()).thenReturn(currentRun);

        CurrentRunDurationGauge sut = new CurrentRunDurationGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});

        Assertions.assertEquals(1, sut.collect().size(), "Expected not to be calculated");
    }


}