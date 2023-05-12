package org.jenkinsci.plugins.prometheus.context;

import com.google.inject.AbstractModule;
import hudson.Extension;
import org.jenkinsci.plugins.prometheus.service.DefaultPrometheusMetrics;
import org.jenkinsci.plugins.prometheus.service.PrometheusMetrics;

@Extension
public class Context extends AbstractModule {

    @Override
    public void configure() {
        bind(PrometheusMetrics.class).to(DefaultPrometheusMetrics.class).in(com.google.inject.Singleton.class);
    }

}
