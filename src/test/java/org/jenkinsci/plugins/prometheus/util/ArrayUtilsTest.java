package org.jenkinsci.plugins.prometheus.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class ArrayUtilsTest {

    @Test
    public void testElementAdded() {
        String[] array = new String[]{"one", "two"};

        String[] newArray = ArrayUtils.appendToArray(array, "three");

        Assertions.assertEquals(3, newArray.length);
        Assertions.assertEquals("three", newArray[2]);
    }
}