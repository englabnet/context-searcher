package net.englab.contextsearcher.models.elastic;

import java.time.Instant;

/**
 * Represents the metadata of the video index.
 * The metadata holds information about the last indexing job.
 *
 * @param startTime     the time when the indexing job was started
 * @param finishTime    the time when the indexing job was finished
 */
public record VideoIndexMetadata(
        Instant startTime,
        Instant finishTime
) {
}
