package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import hudson.model.Computer;
import hudson.model.Node;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class NodesOnlineGauge extends BaseMetricCollector<Jenkins, Gauge> {

    NodesOnlineGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.NODES_ONLINE_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Jenkins nodes online status";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        for (Node node : jenkinsObject.getNodes()) {
            //Check whether the node is online or offline
            Computer comp = node.toComputer();
            if (comp == null) {
                continue;
            }

            if (comp.isOnline()) { // https://javadoc.jenkins.io/hudson/model/Computer.html
                this.collector.labels(node.getNodeName()).set(1);
            } else {
                this.collector.labels(node.getNodeName()).set(0);
            }
        }
    }
}
