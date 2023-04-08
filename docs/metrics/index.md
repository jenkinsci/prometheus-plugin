# Metrics collected by [prometheus-plugin](../../README.md)

Metrics collected by this Plugin are prefixed with "default_jenkins". 
You can change the "default" prefix either via configuration page:

"default" -> default namespace

or an environment variable ```PROMETHEUS_NAMESPACE```. 
If the environment variable is defined this value will be taken.


## DiskUsageCollector

Required Plugin: 
[cloudbees-disk-usage-simple-plugin](https://github.com/jenkinsci/cloudbees-disk-usage-simple-plugin)

| metric                                     | description                                                  | Prometheus Type |
|--------------------------------------------|--------------------------------------------------------------|-----------------|
| default_jenkins_disk_usage_bytes           | Disk usage of first level folder in JENKINS_HOME in bytes    | gauge           |
| default_jenkins_job_usage_bytes            | Amount of disk usage for each job in Jenkins in bytes        | gauge           |
| default_jenkins_file_store_capacity_bytes  | Total size in bytes of the file stores used by Jenkins       | gauge           |
| default_jenkins_file_store_available_bytes | Estimated available space on the file stores used by Jenkins | gauge           |

## ExecutorCollector

| metric                                 | description                                                     | Prometheus Type |
|----------------------------------------|-----------------------------------------------------------------|-----------------|
| default_jenkins_executors_available    | Shows how many Jenkins Executors are available                  | gauge           |
| default_jenkins_executors_busy         | Shows how many Jenkins Executors busy                           | gauge           |
| default_jenkins_executors_connecting   | Shows how many Jenkins Executors are connecting                 | gauge           |
| default_jenkins_executors_defined      | Shows how many Jenkins Executors are defined                    | gauge           |
| default_jenkins_executors_idle         | Shows how many Jenkins Executors are idle                       | gauge           |
| default_jenkins_executors_online       | Shows how many Jenkins Executors are online                     | gauge           |
| default_jenkins_executors_queue_length | Shows number of items that can run but waiting on free executor | gauge           |

## JenkinsStatusCollector

| metric                       | description                                | Prometheus Type |
|------------------------------|--------------------------------------------|-----------------|
| default_jenkins_version      | Shows the jenkins Version                  | info            |
| default_jenkins_up           | Shows if jenkins ready to receive requests | gauge           |
| default_jenkins_uptime       | Shows time since Jenkins was initialized   | gauge           |
| default_jenkins_nodes_online | Shows Nodes online status                  | gauge           |

## JobCollector

Note: Metrics in the table below containing <buildname><build_no:last> need to be enabled via Jenkins Configuration page. Per Build Metrics. The default Metrics 
will just return the last build. You can enable per build metrics in the configuration page (Attention: Performance)

| metric                                                                      | description                                                                                             | Prometheus Type |
|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|-----------------|
| default_jenkins_builds_duration_milliseconds_summary                        | Summary of Jenkins build times in milliseconds by Job                                                   | summary         |
| default_jenkins_builds_success_build_count                                  | Successful build count                                                                                  | counter         |
| default_jenkins_builds_failed_build_count                                   | Failed build count                                                                                      | counter         |
| default_jenkins_builds_health_score                                         | Health score of a job                                                                                   | gauge           |
| default_jenkins_builds_<buildname>_last_build_result_ordinal                | Build status of a job (0=SUCCESS,1=UNSTABLE,2=FAILURE,3=NOT_BUILT,4=ABORTED)                            | gauge           |
| default_jenkins_builds_<buildname>_last_build_result                        | Build status of a job as a boolean value - 0 or 1. Where 0 is: SUCCESS,UNSTABLE and 1: all other States | gauge           |
| default_jenkins_builds_<buildname>_last_build_duration_milliseconds         | Build times in milliseconds of last build                                                               | gauge           |
| default_jenkins_builds_<buildname>_last_build_start_time_milliseconds       | Last build start timestamp in milliseconds                                                              | gauge           |
| default_jenkins_builds_<buildname>_last_build_tests_total                   | Number of total tests during the last build                                                             | gauge           |
| default_jenkins_builds_<buildname>_last_last_build_tests_skipped            | Number of skipped tests during the last build                                                           | gauge           |
| default_jenkins_builds_<buildname>_last_build_tests_failing                 | Number of failing tests during the last build                                                           | gauge           |
| default_jenkins_builds_<buildname>_last_stage_duration_milliseconds_summary | Summary of Jenkins build times by Job and Stage in the last build                                       | summary         |
| default_jenkins_builds_available_builds_count                               | Gauge which indicates how many builds are available for the given job                                   | gauge           |
| default_jenkins_builds_discard_active                                       | Gauge which indicates if the build discard feature is active for the job.                               | gauge           |
| default_jenkins_builds_running_build_duration_milliseconds                  | Gauge which indicates the runtime of the current build.                                                 | gauge           |




