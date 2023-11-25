package net.englab.contextsearcher.models.indexing;

import java.time.Instant;

/**
 * Indexing information which contains the current status of the indexing job and some other additional information.
 *
 * @param startTime     the time the indexing job was started or null if it has not been started yet
 * @param finishTime    the time the indexing job finished or null if it has not finished yet
 * @param status        the current status of the indexing job
 * @param message       the text message that gives additional information regarding the status
 */
public record IndexingInfo(
        Instant startTime,
        Instant finishTime,
        IndexingStatus status,
        String message
) {
    /**
     * Creates empty indexing information. It should be used when
     * the indexing job has not been started yet and the index is empty.
     *
     * @return a new indexing information instance
     */
    public static IndexingInfo none() {
        return new IndexingInfo(null, null, IndexingStatus.NONE, "Index is empty");
    }

    /**
     * Creates indexing information for an indexing job that
     * has been started, but has not finished yet.
     *
     * @param startTime the time when the indexing job was started
     * @return a new indexing information instance
     */
    public static IndexingInfo started(Instant startTime) {
        return new IndexingInfo(startTime, null, IndexingStatus.STARTED, "Indexing is in progress...");
    }

    /**
     * Creates indexing information for a completed indexing job.
     *
     * @param startTime     the time when the indexing job was started
     * @param finishTime    the time when the indexing job completed
     * @return a new indexing information instance
     */
    public static IndexingInfo completed(Instant startTime, Instant finishTime) {
        return new IndexingInfo(startTime, finishTime, IndexingStatus.COMPLETED, "Indexing has been completed");
    }

    /**
     * Creates indexing information for a failed indexing job.
     *
     * @param startTime     the time when the indexing job was started
     * @param finishTime    the time when the indexing job failed
     * @param message       the error message
     * @return a new indexing information instance
     */
    public static IndexingInfo failed(Instant startTime, Instant finishTime, String message) {
        return new IndexingInfo(startTime, finishTime, IndexingStatus.FAILED, message);
    }
}
