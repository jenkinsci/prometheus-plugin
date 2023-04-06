package org.jenkinsci.plugins.prometheus.metrics.jobs;

import hudson.model.RunMap;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.when;


public class NbBuildsGaugeTest extends JobCollectorTest {

    @Mock
    RunMap<WorkflowRun> runMap;

    @Test
    public void testCollectResult() {

        when(runMap.size()).thenReturn(12);
        when(job.getBuildsAsMap()).thenReturn(runMap);

        NbBuildsGauge sut = new NbBuildsGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        validateNames(samples, new String[]{"default_jenkins_builds_available_builds_count"});
        validateSize(samples, 1);
        validateValue(samples.samples.get(0), 12.0);

    }
}