package net.englab.contextsearcher.elastic;

import java.time.Instant;

public record IndexMetadata(
        Instant startTime,
        Instant finishTime
) {
}
