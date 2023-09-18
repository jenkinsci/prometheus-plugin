package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class RegexDisabledMetricTest {

    @Test
    void testDescriptorName() {
        RegexDisabledMetric sut = new RegexDisabledMetric("some_regex");
        Assertions.assertEquals("Regex Entry", sut.getDescriptor().getDisplayName());
    }


}