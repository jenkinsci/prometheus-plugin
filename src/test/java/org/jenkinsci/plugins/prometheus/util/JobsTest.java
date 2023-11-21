package org.jenkinsci.plugins.prometheus.util;

import hudson.model.Job;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
public class JobsTest {

    @Test
    void testEachJob() {
        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class)) {
            Jenkins jenkins = mock(Jenkins.class);
            List<Job> jobs = List.of(mockJob("name1"), mockJob("name2"));
            when(jenkins.getAllItems(Job.class)).thenReturn(jobs);
            jenkinsStatic.when(Jenkins::get).thenReturn(jenkins);


            List<String> names = new ArrayList<>();
            Jobs.forEachJob(job -> names.add(job.getName()));

            Assertions.assertEquals(2, names.size());
            Assertions.assertTrue(names.contains("name1"));
            Assertions.assertTrue(names.contains("name2"));
        }
    }


    private static Job mockJob(String name) {
        Job mock = mock(Job.class);
        when(mock.getName()).thenReturn(name);
        return mock;
    }
}