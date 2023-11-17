package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.common.EnglishVariety;
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

    /**
     * Checks if the given index does not exist.
     *
     * @param index the name of the index to check
     * @return true if the index is absent and false otherwise
     */
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

    /**
     * Creates a new index with the given name.
     *
     * @param index         the name of the index to create
     * @param properties    the mapping properties
     */
    public void createIndex(String index, Map<String, Property> properties) {
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

    /**
     * Removes the index with the given name.
     *
     * @param index the name of the index to remove
     */
    public void removeIndex(String index) {
        try {
            elasticsearchClient.indices().delete(d -> d
                    .index(index)
                    .ignoreUnavailable(true)
            );
        } catch (IOException e) {
            log.error("An exception occurred during index creation", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the metadata of the specified index.
     *
     * @param index the index name
     * @return an Optional containing the index metadata if available
     */
    public Optional<IndexMetadata> getIndexMetadata(String index) {
        try {
            if (isIndexAbsent(index)) {
                return Optional.empty();
            }
            var response = elasticsearchClient.indices()
                    .getMapping(m -> m.index(index));
            return Optional.of(response)
                    .map(this::findAnyIndexMapping)
                    .map(IndexMappingRecord::mappings)
                    .map(TypeMapping::meta)
                    .filter(this::containsMetadata)
                    .map(meta -> new IndexMetadata(
                            meta.get("startTime").to(Instant.class),
                            meta.get("finishTime").to(Instant.class)
                    ));
        } catch (IOException e) {
            log.error("An exception occurred while getting metadata", e);
            throw new RuntimeException(e);
        }
    }

    private IndexMappingRecord findAnyIndexMapping(GetMappingResponse response) {
        return response.result().values().stream()
                .findAny()
                .orElse(null);
    }

    private boolean containsMetadata(Map<String, JsonData> meta) {
        return meta.containsKey("startTime") && meta.containsKey("finishTime");
    }

    /**
     * Sets the index metadata for a given index.
     *
     * @param index     the index name
     * @param metadata  the index metadata
     */
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

    /**
     * Assigns the specified alias to a given Elasticsearch index.
     *
     * @param index the index name
     * @param alias the alias to assign
     */
    public void putAlias(String index, String alias) {
        try {
            elasticsearchClient.indices()
                    .putAlias(a -> a.index(index).name(alias));
        } catch (IOException e) {
            log.error("An exception occurred while setting an alias", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the name of the index associated with the specified alias.
     *
     * @param alias the alias
     * @return an Optional containing the index name if the alias exists
     */
    public Optional<String> getIndexName(String alias) {
        try {
            if (isIndexAbsent(alias)) {
                return Optional.empty();
            }
            var response = elasticsearchClient.indices().get(b -> b.index(alias));
            return response.result().keySet().stream()
                    .findAny();
        } catch (IOException e) {
            log.error("An exception occurred while getting an index name", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the specified video from the index.
     *
     * @param index     the index name
     * @param videoId   the video id
     */
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

    /**
     * Index the given collection of documents.
     *
     * @param index the index name
     * @param docs  the collection of documents
     */
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

    /**
     * Finds video documents in a specified index containing the given phrase.
     *
     * @param index     the name of the index where the search is performed
     * @param phrase    the phrase to search for
     * @param variety   the specific variety of English
     * @param from      the offset from the start of the results
     * @param size      the number of results to be returned
     * @return a SearchResponse object containing the search results
     */
    public SearchResponse<VideoDocument> searchVideoByPhrase(String index, String phrase,
                                                             EnglishVariety variety, int from, int size) {
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
