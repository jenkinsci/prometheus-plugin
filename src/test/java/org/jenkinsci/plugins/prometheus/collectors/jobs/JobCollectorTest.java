package org.jenkinsci.plugins.prometheus.collectors.jobs;


import hudson.model.Job;
import org.jenkinsci.plugins.prometheus.collectors.testutils.CollectorTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class JobCollectorTest extends CollectorTest {

    @Mock
    @SuppressWarnings("rawtypes")
    protected Job job;


}
