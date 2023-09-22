package net.englab.contextsearcher.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

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

    public void indexDocument(String index, String id, Object value) {
        try {
            elasticsearchClient.create(b -> b
                    .index(index)
                    .id(id)
                    .document(value)
            );
        } catch (IOException e) {
            log.error("Exception occurred during doc creation", e);
            throw new RuntimeException(e);
        }
    }

    public SearchResponse<VideoDocument> searchVideoByPhrase(String index, String field, String phrase) {
        try {
            return elasticsearchClient.search(b -> b
                    .index(index)
                    .query(q -> q
                            .matchPhrase(m -> m
                                    .field(field)
                                    .query(phrase)
                            )
                    ).highlight(h -> h
                            .fields(field, f -> f
                                    .numberOfFragments(0)
                            )
                    ), VideoDocument.class);
        } catch (IOException e) {
            log.error("Exception occurred during doc search", e);
            throw new RuntimeException(e);
        }
    }
}
