package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.RunMap;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class NbBuildsGaugeTest extends JobCollectorTest {

    @Mock
    RunMap<WorkflowRun> runMap;

    @Test
    public void testCollectResultUsesApproximateCountOnLegacyCore() {
        when(job.getNextBuildNumber()).thenReturn(13);

        NbBuildsGauge sut = new NbBuildsGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins") {
            @Override
            boolean usesExactBuildCount() {
                return false;
            }
        };

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_builds_available_builds_count"});
        validateMetricFamilySampleSize(samples, 1);
        validateValue(samples.samples.get(0), 12.0);
        verify(job).getNextBuildNumber();
        verify(job, never()).getBuildsAsMap();
    }

    @Test
    public void testCollectResultUsesExactCountOnSafeCore() {
        when(runMap.size()).thenReturn(12);
        when(job.getBuildsAsMap()).thenReturn(runMap);

        NbBuildsGauge sut = new NbBuildsGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins") {
            @Override
            boolean usesExactBuildCount() {
                return true;
            }
        };

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_builds_available_builds_count"});
        validateMetricFamilySampleSize(samples, 1);
        validateValue(samples.samples.get(0), 12.0);
        verify(job).getBuildsAsMap();
        verify(job, never()).getNextBuildNumber();
    }
}