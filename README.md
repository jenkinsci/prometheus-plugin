# Jenkins Prometheus Metrics Plugin

## About
Jenkins Prometheus Plugin expose an endpoint (default `/prometheus`) with metrics where a Prometheus Server can scrape.

## Metrics exposed
Currently only metrics from the [Metrics-plugin](https://github.com/jenkinsci/metrics-plugin) and summary of build
duration of jobs and pipeline stages

## Environment variables

`PROMETHEUS_NAMESPACE` Prefix of metric (Default: `default`).

`PROMETHEUS_ENDPOINT` REST Endpoint (Default: `prometheus`)

## Building

    mvn clean install
    mvn hpi:hpi

## Author / Maintainer
[Lars Sjöström](https://github.com/lsjostro)

### Credits
[fabric8io team](https://github.com/fabric8io). Open.source.ftw++!
