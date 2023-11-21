package org.jenkinsci.plugins.prometheus.collectors.testutils;

import hudson.model.LoadStatistics;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class MockedLoadStatisticSnapshotTest extends CollectorTest {


    @Mock
    protected LoadStatistics.LoadStatisticsSnapshot mock;
}
