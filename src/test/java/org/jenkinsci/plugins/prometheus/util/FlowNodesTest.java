package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowNodesTest {

    @Mock
    private WorkflowRun run;

    @Test
    void returnEmptyListOnPassingNull() {
        when(run.getExecution()).thenReturn(null);
        List<Stage> result = FlowNodes.getSortedStageNodes(run);
        assertEquals(0, result.size());
    }

}
