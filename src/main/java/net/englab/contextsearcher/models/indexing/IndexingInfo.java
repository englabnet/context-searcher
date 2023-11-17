package net.englab.contextsearcher.models.indexing;

import java.time.Instant;

public record IndexingInfo(
        Instant startTime,
        Instant finishTime,
        IndexingStatus status,
        String message
) {
    public static IndexingInfo none() {
        return new IndexingInfo(null, null, IndexingStatus.NONE, "Index is empty");
    }

    public static IndexingInfo started(Instant startTime) {
        return new IndexingInfo(startTime, null, IndexingStatus.STARTED, "Indexing is in progress...");
    }

    public static IndexingInfo completed(Instant startTime, Instant finishTime) {
        return new IndexingInfo(startTime, finishTime, IndexingStatus.COMPLETED, "Indexing has been completed");
    }

    public static IndexingInfo failed(Instant startTime, Instant finishTime, String message) {
        return new IndexingInfo(startTime, finishTime, IndexingStatus.FAILED, message);
    }
}
