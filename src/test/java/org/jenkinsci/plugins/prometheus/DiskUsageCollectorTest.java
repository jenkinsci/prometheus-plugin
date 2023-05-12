package org.jenkinsci.plugins.prometheus;

import com.cloudbees.simplediskusage.DiskItem;
import com.cloudbees.simplediskusage.JobDiskItem;
import com.cloudbees.simplediskusage.QuickDiskUsagePlugin;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import jenkins.model.Jenkins;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DiskUsageCollectorTest {

    @Mock
    private Jenkins jenkins;
    @Mock
    private QuickDiskUsagePlugin quickDiskUsagePlugin;

    private DiskUsageCollector underTest;

    @Before
    public void setUp() {
        underTest = new DiskUsageCollector();
    }

    @Test
    public void shouldNotProduceMetricsWhenDisabled() {
        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(() -> PrometheusConfiguration.get()).thenReturn(config);

            final List<MetricFamilySamples> samples = underTest.collect();

            assertThat(samples, is(empty()));
        }
    }

    @Test
    public void shouldProduceMetrics() throws IOException {
        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        when(config.getCollectDiskUsage()).thenReturn(true);
        try (MockedStatic<ConfigurationUtils> configurationUtils = mockStatic(ConfigurationUtils.class);
             MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class);
             MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class)) {
            configurationUtils.when(() -> ConfigurationUtils.getNamespace()).thenReturn("foo");
            configurationUtils.when(() -> ConfigurationUtils.getSubSystem()).thenReturn("bar");
            configStatic.when(() -> PrometheusConfiguration.get()).thenReturn(config);
            jenkinsStatic.when(() -> Jenkins.get()).thenReturn(jenkins);
            when(jenkins.getPlugin(QuickDiskUsagePlugin.class)).thenReturn(quickDiskUsagePlugin);

            final FileStore store = mock(FileStore.class, "the file store");
            given(store.getTotalSpace()).willReturn(4711L);
            given(store.getUsableSpace()).willReturn(1337L);

            final DiskItem dir = mock(DiskItem.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
            given(dir.getDisplayName()).willReturn("dir name");
            given(dir.getUsage()).willReturn(11L);
            mockFileStore(dir, store);
            final CopyOnWriteArrayList<DiskItem> directories = new CopyOnWriteArrayList<>();
            directories.add(dir);
            given(quickDiskUsagePlugin.getDirectoriesUsages()).willReturn(directories);

            final JobDiskItem job = mock(JobDiskItem.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
            given(job.getFullName()).willReturn("job name");
            given(job.getUrl()).willReturn("/job");
            given(job.getUsage()).willReturn(7L);
            mockFileStore(job, store);
            final CopyOnWriteArrayList<JobDiskItem> jobs = new CopyOnWriteArrayList<>();
            jobs.add(job);
            given(quickDiskUsagePlugin.getJobsUsages()).willReturn(jobs);

            final List<MetricFamilySamples> samples = underTest.collect();

            assertThat(samples, containsInAnyOrder(
                gauges("foo_bar_disk_usage_bytes", containsInAnyOrder(
                    sample(ImmutableMap.of("file_store", "the file store", "directory", "dir"), equalTo(11. * 1024))
                )),
                gauges("foo_bar_job_usage_bytes", containsInAnyOrder(
                    sample(ImmutableMap.of("file_store", "the file store", "jobName", "job name", "url", "/job"), equalTo(7. * 1024))
                )),
                gauges("foo_bar_file_store_capacity_bytes", containsInAnyOrder(
                    sample(ImmutableMap.of("file_store", "the file store"), equalTo(4711.))
                )),
                gauges("foo_bar_file_store_available_bytes", containsInAnyOrder(
                    sample(ImmutableMap.of("file_store", "the file store"), equalTo(1337.))
                ))
            ));
        }
    }

    private static final void mockFileStore(DiskItem item, FileStore store) throws IOException {
        final Path path = item.getPath().toPath().toRealPath();
        when(path.getFileSystem().provider().getFileStore(path)).thenReturn(store);
    }

    private static Matcher<MetricFamilySamples> gauges(String name, Matcher<? super List<MetricFamilySamples.Sample>> samples) {
        requireNonNull(name);
        requireNonNull(samples);

        return new TypeSafeDiagnosingMatcher<MetricFamilySamples>(MetricFamilySamples.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("gauges named ")
                    .appendValue(name)
                    .appendText(" with samples: ")
                    .appendDescriptionOf(samples);
            }

            @Override
            protected boolean matchesSafely(MetricFamilySamples item, Description mismatchDescription) {
                if (!Objects.equal(item.name, name)) {
                    mismatchDescription.appendText("name was ").appendValue(item.name);
                    return false;
                }

                if (item.type != Collector.Type.GAUGE) {
                    mismatchDescription.appendText("type was ").appendValue(item.type);
                    return false;
                }

                if (!samples.matches(item.samples)) {
                    mismatchDescription.appendText("mismatch in samples: ");
                    samples.describeMismatch(item.samples, mismatchDescription);
                    return false;
                }

                return true;
            }
        };
    }

    private static Matcher<MetricFamilySamples.Sample> sample(Map<String, String> labels, Matcher<Double> value) {
        requireNonNull(labels);

        return new TypeSafeDiagnosingMatcher<MetricFamilySamples.Sample>(MetricFamilySamples.Sample.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("sample labeled ")
                    .appendValue(labels)
                    .appendText(" with value ")
                    .appendDescriptionOf(value);
            }

            @Override
            protected boolean matchesSafely(MetricFamilySamples.Sample item, Description mismatchDescription) {
                if (item.labelNames == null) {
                    mismatchDescription.appendText("labelNames was ").appendValue(null);
                    return false;
                }

                if (item.labelValues == null) {
                    mismatchDescription.appendText("labelValues was ").appendValue(null);
                    return false;
                }

                final Map<String, String> actualLabels = new HashMap<>();
                final Iterator<String> names = item.labelNames.iterator();
                final Iterator<String> values = item.labelValues.iterator();
                while (names.hasNext() && values.hasNext()) {
                    actualLabels.put(names.next(), values.next());
                }
                if (names.hasNext() || values.hasNext()) {
                    mismatchDescription.appendText("number of label names doesn't match number of label values");
                }

                if (!actualLabels.equals(labels)) {
                    mismatchDescription.appendText("labels were ").appendValue(actualLabels);
                }

                if (!value.matches(item.value)) {
                    value.describeMismatch(item.value, mismatchDescription);
                    return false;
                }

                return true;
            }
        };
    }
}
