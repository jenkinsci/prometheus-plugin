package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import hudson.model.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class NamedDisabledMetricTest {

    @Test
    void testDescriptorName() {
        NamedDisabledMetric sut = new NamedDisabledMetric("some_metric");
        Descriptor<Entry> descriptor = sut.getDescriptor();
        Assertions.assertEquals("Fully qualified Name Entry", descriptor.getDisplayName());
    }
}