package org.jenkinsci.plugins.prometheus.collectors;

public enum CollectorType {

    JENKINS_UP_GAUGE("up"),
    JENKINS_UPTIME_GAUGE("uptime"),
    JENKINS_VERSION_INFO_GAUGE("version"),
    JENKINS_QUIETDOWN_GAUGE("quietdown"),
    NODES_ONLINE_GAUGE("nodes_online"),
    BUILD_DURATION_GAUGE("build_duration_milliseconds"),
    BUILD_LOGFILE_SIZE_GAUGE("build_logfile_size_bytes"),
    BUILD_DURATION_SUMMARY("duration_milliseconds_summary"),
    BUILD_RESULT_GAUGE("build_result"),
    BUILD_RESULT_ORDINAL_GAUGE("build_result_ordinal"),
    BUILD_START_GAUGE("build_start_time_milliseconds"),
    BUILD_FAILED_COUNTER("failed_build_count"),
    BUILD_TOTAL_COUNTER("total_build_count"),
    BUILD_SUCCESSFUL_COUNTER("success_build_count"),
    BUILD_UNSTABLE_COUNTER("unstable_build_count"),
    BUILD_ABORTED_COUNTER("aborted_build_count"),
    BUILD_LIKELY_STUCK_GAUGE("likely_stuck"),

    FAILED_TESTS_GAUGE("build_tests_failing"),
    SKIPPED_TESTS_GAUGE("last_build_tests_skipped"),
    STAGE_SUMMARY("stage_duration_milliseconds_summary"),
    STAGE_BUILDRESULT_ORDINAL("stage_result_ordinal"),
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
    DISK_USAGE_FILE_COUNT_GAUGE("disk_usage_file_count"),
    FILE_STORE_AVAILABLE_GAUGE("file_store_available_bytes"),
    FILE_STORE_CAPACITY_GAUGE("file_store_capacity_bytes"),
    JOB_USAGE_BYTES_GAUGE("job_usage_bytes"),

    BUILD_FAILED_TESTS("build_tests_failing"),

    COVERAGE_CLASS_COVERED("coverage_class_covered"),
    COVERAGE_CLASS_MISSED("coverage_class_missed"),
    COVERAGE_CLASS_TOTAL("coverage_class_total"),
    COVERAGE_CLASS_PERCENT("coverage_class_percent"),

    COVERAGE_BRANCH_COVERED("coverage_branch_covered"),
    COVERAGE_BRANCH_MISSED("coverage_branch_missed"),
    COVERAGE_BRANCH_TOTAL("coverage_branch_total"),
    COVERAGE_BRANCH_PERCENT("coverage_branch_percent"),

    COVERAGE_INSTRUCTION_COVERED("coverage_instruction_covered"),
    COVERAGE_INSTRUCTION_MISSED("coverage_instruction_missed"),
    COVERAGE_INSTRUCTION_TOTAL("coverage_instruction_total"),
    COVERAGE_INSTRUCTION_PERCENT("coverage_instruction_percent"),

    COVERAGE_FILE_COVERED("coverage_file_covered"),
    COVERAGE_FILE_MISSED("coverage_file_missed"),
    COVERAGE_FILE_TOTAL("coverage_file_total"),
    COVERAGE_FILE_PERCENT("coverage_file_percent"),

    COVERAGE_LINE_COVERED("coverage_line_covered"),
    COVERAGE_LINE_MISSED("coverage_line_missed"),
    COVERAGE_LINE_TOTAL("coverage_line_total"),
    COVERAGE_LINE_PERCENT("coverage_line_percent"),

    JOB_LOG_UPDATED_GAUGE("job_log_updated");

    private final String name;

    CollectorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
