package org.jenkinsci.plugins.prometheus.collectors.coverage;

import edu.hm.hafner.coverage.Coverage;
import edu.hm.hafner.coverage.Metric;
import io.jenkins.plugins.coverage.metrics.model.Baseline;
import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.when;

public abstract class CoverageTest extends MockedRunCollectorTest {

    private final Baseline baseline;
    private final Metric metric;

    public CoverageTest(Baseline baseline, Metric metric) {
        this.baseline = baseline;
        this.metric = metric;
    }


    protected void setUpSuccessfulMocksForCovered() {
        CoverageBuildAction mockedCoverageBuildAction = Mockito.mock(CoverageBuildAction.class);
        Coverage mockedCoverage = Mockito.mock(Coverage.class);
        when(mockedCoverage.getMetric()).thenReturn(metric);
        when(mockedCoverage.getCovered()).thenReturn(10);
        when(mockedCoverageBuildAction.getAllValues(baseline)).thenReturn(List.of(mockedCoverage));
        when(mock.getAction(CoverageBuildAction.class)).thenReturn(mockedCoverageBuildAction);
    }

    protected void setUpSuccessfulMocksForMissed() {
        CoverageBuildAction mockedCoverageBuildAction = Mockito.mock(CoverageBuildAction.class);
        Coverage mockedCoverage = Mockito.mock(Coverage.class);
        when(mockedCoverage.getMetric()).thenReturn(metric);
        when(mockedCoverage.getMissed()).thenReturn(5);
        when(mockedCoverageBuildAction.getAllValues(baseline)).thenReturn(List.of(mockedCoverage));
        when(mock.getAction(CoverageBuildAction.class)).thenReturn(mockedCoverageBuildAction);
    }

    protected void setUpSuccessfulMocksForTotal() {
        CoverageBuildAction mockedCoverageBuildAction = Mockito.mock(CoverageBuildAction.class);
        Coverage mockedCoverage = Mockito.mock(Coverage.class);
        when(mockedCoverage.getMetric()).thenReturn(metric);
        when(mockedCoverage.getTotal()).thenReturn(15);
        when(mockedCoverageBuildAction.getAllValues(baseline)).thenReturn(List.of(mockedCoverage));
        when(mock.getAction(CoverageBuildAction.class)).thenReturn(mockedCoverageBuildAction);
    }



    protected void setUpUnsuccessfulMocks() {
        CoverageBuildAction mockedCoverageBuildAction = Mockito.mock(CoverageBuildAction.class);
        Coverage mockedCoverage = Mockito.mock(Coverage.class);
        when(mockedCoverageBuildAction.getAllValues(baseline)).thenReturn(List.of(mockedCoverage));
        when(mock.getAction(CoverageBuildAction.class)).thenReturn(mockedCoverageBuildAction);
    }

}
