package org.jenkinsci.plugins.prometheus.collectors.jobs;


import hudson.model.Job;
import org.jenkinsci.plugins.prometheus.collectors.testutils.CollectorTest;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class JobCollectorTest extends CollectorTest {

    @Mock
    protected Job job;


}
