package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.console.AnnotatedLargeText;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuildLogFileSizeGaugeTest extends MockedRunCollectorTest {

    @Test
    public void testNothingCalculatedWhenRunIsBuilding() {

        when(mock.isBuilding()).thenReturn(true);

        BuildLogFileSizeGauge sut = new BuildLogFileSizeGauge(getLabelNames(), getNamespace(), getSubSystem(), "default");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        assertEquals(1, collect.size());
        assertEquals(0, collect.get(0).samples.size(), "Would expect no sample created when run is running");
    }

    @Test
    public void testCollectResult() {

        when(mock.isBuilding()).thenReturn(false);
        AnnotatedLargeText annotatedLargeText = Mockito.mock(AnnotatedLargeText.class);
        when(annotatedLargeText.length()).thenReturn(3000L);

        when(mock.getLogText()).thenReturn(annotatedLargeText);

        BuildLogFileSizeGauge sut = new BuildLogFileSizeGauge(getLabelNames(), getNamespace(), getSubSystem(), "default");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        assertEquals(1, collect.size());
        assertEquals(3000.0, collect.get(0).samples.get(0).value);

    }
}