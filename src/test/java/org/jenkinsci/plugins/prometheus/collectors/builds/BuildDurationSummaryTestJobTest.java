package org.jenkinsci.plugins.prometheus.collectors.builds;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


@ExtendWith(MockitoExtension.class)
public class BuildDurationSummaryTestJobTest extends MockedRunCollectorTest {

    @Test
    public void testNothingCalculatedWhenRunIsBuilding() {

        Mockito.when(mock.isBuilding()).thenReturn(true);

        BuildDurationSummary sut = new BuildDurationSummary(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size(), "Would expect no sample created when run is running");
    }


    @Test
    public void testCollectResult() {
        Mockito.when(mock.isBuilding()).thenReturn(false);
        Mockito.when(mock.getDuration()).thenReturn(1000L);

        BuildDurationSummary sut = new BuildDurationSummary(getLabelNames(), getNamespace(), getSubSystem());

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());

        Assertions.assertEquals(3, collect.get(0).samples.size(), "Would expect one result");

        for (Collector.MetricFamilySamples.Sample sample : collect.get(0).samples) {
            if (sample.name.equals("default_jenkins_builds_duration_milliseconds_summary_count")) {
                Assertions.assertEquals(1.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_duration_milliseconds_summary_sum")) {
                Assertions.assertEquals(1000.0, sample.value);
            }
            if (sample.name.equals("default_jenkins_builds_duration_milliseconds_summary_created")) {
                Assertions.assertTrue(sample.value > 0);
            }
        }
    }
}