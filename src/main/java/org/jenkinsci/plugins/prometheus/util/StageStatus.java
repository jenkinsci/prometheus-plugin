package org.jenkinsci.plugins.prometheus.util;

/**
 * Status of a pipeline stage.
 * <p>
 * The declaration order is part of the public metric contract: the ordinal is exported by
 * {@code stage_result_ordinal}, so do not reorder or insert values.
 */
public enum StageStatus {
    NOT_EXECUTED,
    ABORTED,
    SUCCESS,
    IN_PROGRESS,
    PAUSED_PENDING_INPUT,
    FAILED,
    UNSTABLE
}
