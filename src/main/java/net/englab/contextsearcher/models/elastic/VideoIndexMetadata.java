package net.englab.contextsearcher.models.elastic;

import co.elastic.clients.json.JsonData;

import java.time.Instant;
import java.util.Map;

/**
 * Represents the metadata of the video index.
 * The metadata holds information about the last indexing job.
 *
 * @param startTime     the time when the indexing job was started
 * @param finishTime    the time when the indexing job was finished
 */
public record VideoIndexMetadata(Instant startTime, Instant finishTime) {

    /**
     * Creates new video index metadata based on the metadata map.
     *
     * @param metadata a metadata map from Elasticsearch
     */
    public VideoIndexMetadata(Map<String, JsonData> metadata) {
        this(getInstant(metadata, "startTime"), getInstant(metadata, "finishTime"));
    }

    private static Instant getInstant(Map<String, JsonData> meta, String key) {
        JsonData value = meta.get(key);
        if (value == null) return null;
        return value.to(Instant.class);
    }

    /**
     * Convert the current instance to a metadata map for Elasticsearch.
     *
     * @return a metadata map for Elasticsearch
     */
    public Map<String, JsonData> toMetadata() {
        return Map.of(
                "startTime", JsonData.of(startTime),
                "finishTime", JsonData.of(finishTime)
        );
    }
}
