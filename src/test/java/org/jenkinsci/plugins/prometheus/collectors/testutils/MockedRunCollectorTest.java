package org.jenkinsci.plugins.prometheus.collectors.testutils;

import hudson.model.Run;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class MockedRunCollectorTest extends CollectorTest {

    @Mock
    protected Run<?,?> mock;


}
