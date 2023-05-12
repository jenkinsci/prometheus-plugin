package org.jenkinsci.plugins.prometheus.collectors.testutils;

import hudson.model.LoadStatistics;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class MockedLoadStatisticSnapshotTest extends CollectorTest {


    @Mock
    protected LoadStatistics.LoadStatisticsSnapshot mock;
}
