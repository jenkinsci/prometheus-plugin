package org.jenkinsci.plugins.prometheus.service;

import hudson.ExtensionPoint;

public interface PrometheusMetrics extends ExtensionPoint {

    String getMetrics();

    void collectMetrics();

}
