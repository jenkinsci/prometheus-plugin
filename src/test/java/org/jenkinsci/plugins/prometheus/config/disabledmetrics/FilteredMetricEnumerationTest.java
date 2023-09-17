package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import io.prometheus.client.Collector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mockStatic;

public class FilteredMetricEnumerationTest {

    @Test
    void testFilterMatches() {
        try (MockedStatic<MetricStatusChecker> statusCheckerMockedStatic = mockStatic(MetricStatusChecker.class)) {

            statusCheckerMockedStatic.when(() -> MetricStatusChecker.isEnabled("metric_1")).thenReturn(false);
            statusCheckerMockedStatic.when(() -> MetricStatusChecker.isEnabled("metric_2")).thenReturn(true);

            List<Collector.MetricFamilySamples> list = List.of(
                    new Collector.MetricFamilySamples("metric_1", Collector.Type.GAUGE, "help1", List.of()),
                    new Collector.MetricFamilySamples("metric_2", Collector.Type.GAUGE, "help2", List.of())
            );

            Iterator<Collector.MetricFamilySamples> iterator = list.iterator();


            FilteredMetricEnumeration filteredMetricEnumeration = new FilteredMetricEnumeration(iterator);

            int foundElements = 0;
            String foundKey = "";
            while (filteredMetricEnumeration.hasMoreElements()) {
                Collector.MetricFamilySamples familySamples = filteredMetricEnumeration.nextElement();
                foundKey = familySamples.name;
                foundElements++;
            }
            Assertions.assertEquals(1, foundElements);
            Assertions.assertEquals("metric_2", foundKey);

         }
    }
}