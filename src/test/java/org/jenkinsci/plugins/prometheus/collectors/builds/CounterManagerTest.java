package org.jenkinsci.plugins.prometheus.collectors.builds;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;

public class CounterManagerTest extends MockedRunCollectorTest {
    private CounterManager manager;

    public CounterManagerTest() {
        manager = CounterManager.getManager();
    }
    
    @Test
    public void TestEquivalentEntryReturnsCounter() {
        String[] labels = new String[] { "TestLabel" };

        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration config = mock(PrometheusConfiguration.class);
            when(config.getDefaultNamespace()).thenReturn(getNamespace());
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            var retrievedCounter = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, labels, null);
            var retrievedCounter2 = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, labels, null);

            // Should be a value reference comparison. They should be the exact same
            // MetricCollector.
            Assert.assertEquals(retrievedCounter, retrievedCounter2);
        }
    }

    @Test
    public void TestNamespaceChangeReturnsNewCounter() {
        String[] labels = new String[] { "TestLabel" };

        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration config = mock(PrometheusConfiguration.class);
            
            // Use default namespace for one counter
            when(config.getDefaultNamespace()).thenReturn(getNamespace());
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            var retrievedCounter = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, labels, null);

            // Second counter returns modified namespace
            when(config.getDefaultNamespace()).thenReturn("modified_namespace");
            var retrievedCounter2 = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, labels, null);

            // Should be a value reference comparison. They should not be the same metric since the namespace has changed.
            Assert.assertNotEquals(retrievedCounter, retrievedCounter2);
        }
    }

    @Test
    public void TestLabelChangeReturnsNewCounter(){
          String[] label1 = new String[]{"labels"};
          String[] label2 = Arrays.copyOf(label1, 2);
          label2[1] = "Hi";
         try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration config = getMockCounterManagerConfig();
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            var retrievedCounter = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, label1, null);
            var retrievedCounter2 = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, label2, null);

            // Should be a value reference comparison. They should be different since labels differ.
            Assert.assertNotEquals(retrievedCounter, retrievedCounter2);
        }
    }

    @Test
    public void TestPrefixChangeReturnsNewCounter(){
          String[] label1 = new String[]{"labels"};
    
         try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration config = getMockCounterManagerConfig();
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            var retrievedCounter = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, label1, "yes");
            var retrievedCounter2 = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, label1, null);

            // Should be a value reference comparison. They should not be the same since prefix changed
            Assert.assertNotEquals(retrievedCounter, retrievedCounter2);
        }
    }

    @Test
    public void TestDifferentCounterReturnsUniqueCounter(){
          String[] label1 = new String[]{"labels"};
    
         try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            PrometheusConfiguration config = getMockCounterManagerConfig();
            configStatic.when(PrometheusConfiguration::get).thenReturn(config);
            var retrievedCounter = manager.getCounter(CollectorType.BUILD_ABORTED_COUNTER, label1, null);
            var retrievedCounter2 = manager.getCounter(CollectorType.BUILD_SUCCESSFUL_COUNTER, label1, null);

            // Should be a value reference comparison. They should not be the same since the collector type differs.
            Assert.assertNotEquals(retrievedCounter, retrievedCounter2);
        }
    }

    public PrometheusConfiguration getMockCounterManagerConfig(){
         PrometheusConfiguration config = mock(PrometheusConfiguration.class);
         when(config.getDefaultNamespace()).thenReturn(getNamespace());

         return config;
    }
}
