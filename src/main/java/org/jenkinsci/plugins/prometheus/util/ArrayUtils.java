package org.jenkinsci.plugins.prometheus.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {

    public static String[] appendToArray(String[] array, String newValue) {
        if (array == null) {
            return null;
        }
        List<String> tempList = new ArrayList<>(Arrays.asList(array));
        tempList.add(newValue);
        return tempList.toArray(new String[0]);
    }
}
