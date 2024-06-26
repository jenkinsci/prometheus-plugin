# Plugin configuration

## Path
With the path configuration you can configure under which url the Prometheus page will be rendered. The default
is `prometheus` which will cause the rending to be at `http(s)://yourInstance:(port)/(jenkins?)/prometheus`

## Default Namespace
You can configure a namespace for all metrics generated by this plugin which will be prefixed to the output

## Enable authentication for prometheus end-point
If you check this the accessing the endpoint needs to have the `Metrics.VIEW` permission

## Collecting metrics period in second
The metrics are collected every x seconds. You can configure the interval here.

## Count duration of successful builds
If checked the plugin wil calculate the duration of successful builds

## Count duration of unstable builds
If checked the plugin wil calculate the duration of unstable builds

## Count duration of not-built builds
If checked the plugin wil calculate the duration of not-built builds

## Count duration of aborted builds
If checked the plugin wil calculate the duration of aborted builds

## Fetch the test results of builds
If checked the plugin will return test results as metrics

## Add build parameter label to metrics
If checked the plugin will add the build (input) parameters to the metrics. 
Note:  If the cardinality of your build parameters is high, this will create a large number of metrics. 
This can result in poor jenkins and prometheus performance.

## Add build status label to metrics
If checked the plugin will add the build status like SUCCESS, FAILURE, RUNNING to the metrics of
`duration_milliseconds_summary`, `success_build_count` and `failed_build_count metrics`

## Job attribute name
You can configure the label of the field which contains the job name here.

## Build parameters that will be added as separate labels to metrics
Please refer online documentation (question mark symbol in jenkins configuration page)

## Collect disk usage
If checked the plugin will collect the disk usage of your master agent. Do not use this on a cloud storage provider.

## Collect node status
If checked the plugin will collect data of up/down status of your Jenkins agents

## Collect metrics for each run per build
If checked it will cause the plugin to add metrics for every build available. The build number will be added as label. Use with caution!

## Collect Code coverage  (since v2.3.0)
If checked and you publish your code coverage results with [https://plugins.jenkins.io/coverage](https://plugins.jenkins.io/coverage)
the plugin will output metrics for: 
* (Class | Branch | Instruction | File | Line ) Missed
* (Class | Branch | Instruction | File | Line ) Covered
* (Class | Branch | Instruction | File | Line ) Total
* (Class | Branch | Instruction | File | Line ) Percent

## Disable Metrics (since v2.3.0)
Sometimes you don't need all metrics in your prometheus endpoint which this plugin provides. 
You can disable certain metrics. These metrics are not being collected by the plugin and therefore not added in the 
prometheus endpoint.

![img.png](img/disabled_metrics.png)

### Regex Entry
A Regex entry can be used to disable a group of metrics. E.g. if you want to disable everything with 
default_jenkins_disk.*

### Fully qualified Name Entry
If you want to disable certain individual entries you can do it with this entry. The value should be the same
as you can see it in the prometheus endpoint. It's case-insensitive.
