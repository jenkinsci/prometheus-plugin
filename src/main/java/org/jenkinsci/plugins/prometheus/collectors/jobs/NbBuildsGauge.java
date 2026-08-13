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
        return "Number of builds available for this job. On Jenkins cores with LazyLoadRunMapEntrySet, this falls "
                + "back to the highest assigned build number to avoid a core deadlock during collection.";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {
        lock.readLock().lock();
        try {
            int nbBuilds = usesExactBuildCount()
                    ? jenkinsObject.getBuildsAsMap().size()
                    : Math.max(0, jenkinsObject.getNextBuildNumber() - 1);
            this.collector.labels(labelValues).set(nbBuilds);
        } finally {
            lock.readLock().unlock();
        }
    }

    boolean usesExactBuildCount() {
        return !hasLazyLoadRunMapEntrySet();
    }

    private static boolean hasLazyLoadRunMapEntrySet() {
        try {
            Class.forName("jenkins.model.lazy.LazyLoadRunMapEntrySet");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
