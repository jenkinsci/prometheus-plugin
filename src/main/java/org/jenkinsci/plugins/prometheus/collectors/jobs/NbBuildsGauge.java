package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NbBuildsGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected NbBuildsGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.NB_BUILDS_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Number of builds available for this job";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {
        lock.readLock().lock();
        try  {
            // Avoid calling getBuildsAsMap().size() which forces a full lazy-load and causes deadlock
            // with Jenkins build discarder on core < 2.529. Use getNextBuildNumber() - 1 instead,
            // which gives us the count without loading the entire build map.
            // See: https://github.com/jenkinsci/prometheus-plugin/issues/832
            int nbBuilds = jenkinsObject.getNextBuildNumber() - 1;
            this.collector.labels(labelValues).set(nbBuilds);
        } finally {
            lock.readLock().unlock();
        }
    }
}
