package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class BuildCompletionListenerTest {

    @Test
    @Issue("#643")
    void unregisterClearsRunStack() {
        Run<?,?> mock = mock(Run.class);
        TaskListener taskListener = mock(TaskListener.class);

        BuildCompletionListener sut = BuildCompletionListener.getInstance();

        sut.onCompleted(mock, taskListener);

        assertEquals(1, sut.getRunStack().size());

        sut.unregister();

        assertEquals(0, sut.getRunStack().size(), "Unregister should clear the list. Otherwise a memory leak can occur.");

    }
}