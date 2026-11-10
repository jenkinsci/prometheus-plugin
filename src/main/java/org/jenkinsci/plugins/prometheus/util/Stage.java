package org.jenkinsci.plugins.prometheus.util;

/**
 * Summary of a single stage in a pipeline run.
 *
 * @param id             id of the flow node that starts the stage
 * @param name           display name of the stage
 * @param status         status of the stage
 * @param durationMillis total duration of the stage, including time paused for input
 */
public record Stage(String id, String name, StageStatus status, long durationMillis) {
}
