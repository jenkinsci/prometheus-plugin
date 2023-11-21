package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedJenkinsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class JenkinsVersionInfoTest extends MockedJenkinsTest {

    @Test
    public void testCollectResult() throws Exception {


        setFinalStaticTo123(Jenkins.class.getDeclaredField("VERSION"));


        JenkinsVersionInfo sut = new JenkinsVersionInfo(new String[]{}, getNamespace(), getSubSystem());
        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);

        System.out.println(samples);
        validateNames(samples, new String[]{"default_jenkins_version_info", "default_jenkins_version"});
        validateMetricFamilySampleSize(samples, 1);
        validateHelp(samples, "Jenkins Application Version");
        validateValue(samples, 0, 1.0);


        Assertions.assertEquals("version", samples.samples.get(0).labelNames.get(0));
        Assertions.assertEquals("123", samples.samples.get(0).labelValues.get(0));

    }

    static void setFinalStaticTo123(Field field) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, "123");
    }

}