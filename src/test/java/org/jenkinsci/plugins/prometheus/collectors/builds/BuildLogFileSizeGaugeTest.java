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

@ExtendWith(MockitoExtension.class)
class BuildLogFileSizeGaugeTest extends MockedRunCollectorTest {

    @Test
    public void testNothingCalculatedWhenRunIsBuilding() {

        Mockito.when(mock.isBuilding()).thenReturn(true);

        BuildLogFileSizeGauge sut = new BuildLogFileSizeGauge(getLabelNames(), getNamespace(), getSubSystem(), "default");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size(), "Would expect no sample created when run is running");
    }

    @Test
    public void testCollectResult() {

        Mockito.when(mock.isBuilding()).thenReturn(false);
        AnnotatedLargeText annotatedLargeText = Mockito.mock(AnnotatedLargeText.class);
        Mockito.when(annotatedLargeText.length()).thenReturn(3000L);

        Mockito.when(mock.getLogText()).thenReturn(annotatedLargeText);

        BuildLogFileSizeGauge sut = new BuildLogFileSizeGauge(getLabelNames(), getNamespace(), getSubSystem(), "default");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());

        Assertions.assertEquals(3000.0, collect.get(0).samples.get(0).value);

    }
}