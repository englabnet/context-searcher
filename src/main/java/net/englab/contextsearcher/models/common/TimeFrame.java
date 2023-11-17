package net.englab.contextsearcher.models.common;

/**
 * Represents a time frame with a start and end time, usually stored in seconds.
 *
 * @param startTime the start time of the time frame
 * @param endTime   the end time of the time frame
 */
public record TimeFrame(double startTime, double endTime) {}
