package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.elastic.IndexMetadata;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticService {

    private final ElasticsearchClient elasticsearchClient;

    public boolean isIndexAbsent(String index) {
        try {
            var response = elasticsearchClient.indices()
                    .exists(b -> b.index(index));
            return !response.value();
        } catch (IOException e) {
            log.error("An exception occurred while checking the existence of an index", e);
            throw new RuntimeException(e);
        }
    }

    public void createIndexIfAbsent(String index, Map<String, Property> properties) {
        try {
            if (isIndexAbsent(index)) {
                elasticsearchClient.indices()
                        .create(b -> b
                                .index(index)
                                .mappings(m -> m
                                        .properties(properties)
                                        .dynamic(DynamicMapping.Strict)
                                )
                        );
            }
        } catch (IOException e) {
            log.error("An exception occurred during index creation", e);
            throw new RuntimeException(e);
        }
    }

    public void removeIndex(String index) {
        try {
            elasticsearchClient.indices().delete(d -> d.index(index).ignoreUnavailable(true));
        } catch (IOException e) {
            log.error("An exception occurred during index creation", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<IndexMetadata> getIndexMetadata(String index) {
        try {
            if (isIndexAbsent(index)) return Optional.empty();
            var response = elasticsearchClient.indices()
                    .getMapping(m -> m.index(index));
            return Optional.of(response)
                    .map(r -> r.get(index))
                    .map(IndexMappingRecord::mappings)
                    .map(TypeMapping::meta)
                    .filter(meta -> meta.containsKey("startTime") && meta.containsKey("finishTime"))
                    .map(meta -> new IndexMetadata(
                            meta.get("startTime").to(Instant.class),
                            meta.get("finishTime").to(Instant.class)
                    ));
        } catch (IOException e) {
            log.error("An exception occurred while getting metadata", e);
            throw new RuntimeException(e);
        }
    }

    public void setIndexMetadata(String index, IndexMetadata metadata) {
        try {
            elasticsearchClient.indices().putMapping(m -> m
                    .index(index)
                    .meta("startTime", JsonData.of(metadata.startTime()))
                    .meta("finishTime", JsonData.of(metadata.finishTime()))
            );
        } catch (IOException e) {
            log.error("An exception occurred while setting metadata", e);
            throw new RuntimeException(e);
        }
    }

    public void removeVideo(String index, String videoId) {
        try {
            elasticsearchClient.deleteByQuery(d -> d
                    .index(index)
                    .query(q -> q
                            .term(t -> t
                                    .field("video_id")
                                    .value(videoId)
                            )
                    )
            );
        } catch (IOException e) {
            log.error("An exception occurred during video removal", e);
            throw new RuntimeException(e);
        }
    }

    @Async
    public CompletableFuture<BulkResponse> indexDocuments(String index, Collection<?> docs) {
        List<BulkOperation> bulkOperations = docs.stream()
                .map(doc -> BulkOperation.of(b -> b
                        .create(c -> c
                                .id(UUID.randomUUID().toString())
                                .document(doc))
                        )
                ).toList();
        try {
            BulkResponse response = elasticsearchClient.bulk(b -> b
                    .index(index)
                    .operations(bulkOperations)
            );
            return CompletableFuture.completedFuture(response);
        } catch (IOException e) {
            log.error("An exception occurred while indexing documents", e);
            throw new RuntimeException(e);
        }
    }

    public SearchResponse<VideoDocument> searchVideoByPhrase(String index, String phrase, EnglishVariety variety, int from, int size) {
        try {
            return elasticsearchClient.search(b -> b
                    .index(index)
                    .from(from)
                    .size(size)
                    .query(buildVideoQuery(phrase, variety)._toQuery())
                    .highlight(h -> h
                            .fields("sentence", f -> f
                                    .numberOfFragments(0)
                            )
                    ), VideoDocument.class);
        } catch (IOException e) {
            log.error("An exception occurred during doc search", e);
            throw new RuntimeException(e);
        }
    }

    private static BoolQuery buildVideoQuery(String phrase, EnglishVariety variety) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
                .must(m -> m.matchPhrase(p -> p.field("sentence").query(phrase)));
        if (variety != EnglishVariety.ALL) {
            builder.filter(f -> f.term(t -> t.field("variety").value(variety.name())));
        }
        return builder.build();
    }
}
