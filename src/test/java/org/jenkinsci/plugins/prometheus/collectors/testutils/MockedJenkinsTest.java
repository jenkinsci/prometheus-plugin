package org.jenkinsci.plugins.prometheus.collectors.testutils;

import hudson.model.LoadStatistics;
import jenkins.model.Jenkins;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class MockedJenkinsTest extends CollectorTest {


    @Mock
    protected Jenkins mock;
}
