package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticService {

    private final ElasticsearchClient elasticsearchClient;

    public void createIndexIfAbsent(String index, Map<String, Property> properties) {
        try {
            BooleanResponse booleanResponse = elasticsearchClient.indices().exists(b -> b.index(index));
            if (!booleanResponse.value()) {
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
            log.error("Exception occurred during index creation", e);
            throw new RuntimeException(e);
        }
    }

    public void removeIndex(String index) {
        try {
            elasticsearchClient.indices().delete(d -> d.index(index));
        } catch (IOException e) {
            log.error("Exception occurred during index creation", e);
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
            log.error("Exception occurred during video removal", e);
            throw new RuntimeException(e);
        }
    }

    @Async
    public Future<BulkResponse> indexDocuments(String index, Collection<?> docs) {
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
            log.error("Exception occurred while indexing documents", e);
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
            log.error("Exception occurred during doc search", e);
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
