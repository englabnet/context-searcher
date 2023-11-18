package net.englab.contextsearcher.models.elastic;

import java.time.Instant;

public record IndexMetadata(
        Instant startTime,
        Instant finishTime
) {
}
