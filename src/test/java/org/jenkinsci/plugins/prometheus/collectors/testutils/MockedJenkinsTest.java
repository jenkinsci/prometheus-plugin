package org.jenkinsci.plugins.prometheus.collectors.testutils;

import jenkins.model.Jenkins;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class MockedJenkinsTest extends CollectorTest {


    @Mock
    protected Jenkins mock;
}
