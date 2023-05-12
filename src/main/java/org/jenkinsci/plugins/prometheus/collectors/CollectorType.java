package org.jenkinsci.plugins.prometheus.collectors;

public enum CollectorType {

    JENKINS_UP_GAUGE("up"),
    JENKINS_UPTIME_GAUGE("uptime"),
    JENKINS_VERSION_INFO_GAUGE("version"),
    NODES_ONLINE_GAUGE("nodes_online"),
    BUILD_DURATION_GAUGE("build_duration_milliseconds"),
    BUILD_DURATION_SUMMARY("duration_milliseconds_summary"),
    BUILD_FAILED_COUNTER("failed_build_count"),
    BUILD_RESULT_GAUGE("build_result"),
    BUILD_RESULT_ORDINAL_GAUGE("build_result_ordinal"),
    BUILD_START_GAUGE("build_start_time_milliseconds"),
    BUILD_SUCCESSFUL_COUNTER("success_build_count"),
    FAILED_TESTS_GAUGE("build_tests_failing"),
    SKIPPED_TESTS_GAUGE("last_build_tests_skipped"),
    STAGE_SUMMARY("stage_duration_milliseconds_summary"),
    TOTAL_TESTS_GAUGE("build_tests_total"),
    HEALTH_SCORE_GAUGE("health_score"),
    NB_BUILDS_GAUGE("available_builds_count"),
    BUILD_DISCARD_GAUGE("discard_active"),
    CURRENT_RUN_DURATION_GAUGE("running_build_duration_milliseconds"),
    EXECUTORS_AVAILABLE_GAUGE("available"),
    EXECUTORS_BUSY_GAUGE("busy"),
    EXECUTORS_CONNECTING_GAUGE("connecting"),
    EXECUTORS_DEFINED_GAUGE("defined"),
    EXECUTORS_IDLE_GAUGE("idle"),
    EXECUTORS_ONLINE_GAUGE("online"),
    EXECUTORS_QUEUE_LENGTH_GAUGE("queue_length"),

    DISK_USAGE_BYTES_GAUGE("disk_usage_bytes"),
    FILE_STORE_AVAILABLE_GAUGE("file_store_available_bytes"),
    FILE_STORE_CAPACITY_GAUGE("file_store_capacity_bytes"),
    JOB_USAGE_BYTES_GAUGE("job_usage_bytes"),

    BUILD_FAILED_TESTS("build_tests_failing");

    private final String name;

    CollectorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
