package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import hudson.model.Computer;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedJenkinsTest;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JenkinsUptimeGaugeTest extends MockedJenkinsTest {

    @Test
    public void testCollectResult() {

        try (MockedStatic<Clock> clockMockedStatic = Mockito.mockStatic(Clock.class)) {

            Clock mockedClock = mock(Clock.class);
            clockMockedStatic.when(Clock::systemUTC).thenReturn(mockedClock);
            when(mockedClock.millis()).thenReturn(2000L);
            Computer computerMock = mock(Computer.class);

            when(computerMock.getConnectTime()).thenReturn(500L);

            when(mock.toComputer()).thenReturn(computerMock);

            JenkinsUptimeGauge sut = new JenkinsUptimeGauge(new String[]{}, getNamespace(), getSubSystem());
            sut.calculateMetric(mock, getLabelValues());

            List<Collector.MetricFamilySamples> collect = sut.collect();

            validateMetricFamilySampleListSize(collect, 1);

            Collector.MetricFamilySamples samples = collect.get(0);

            validateNames(samples, new String[]{"default_jenkins_uptime"});
            validateMetricFamilySampleSize(samples, 1);
            validateHelp(samples, "Time since Jenkins machine was initialized");
            validateValue(samples, 0, 1500.0);
        }
    }

}