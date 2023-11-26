package net.englab.contextsearcher.services.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides operations that are related to Elasticsearch documents
 * such as indexing and deletion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticDocumentManager {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * Asynchronously indexes the given collection of documents. The method returns immediately,
     * providing a CompletableFuture for the eventual operation result.
     *
     * @param indexName the name of the index to which the documents are to be added
     * @param docs      the collection of documents to be indexed
     * @return  a CompletableFuture that, upon completion, contains a BulkResponse
     *          detailing the result of the bulk indexing operation
     */
    @Async
    public CompletableFuture<BulkResponse> index(String indexName, Collection<?> docs) {
        List<BulkOperation> bulkOperations = docs.stream()
                .map(doc -> BulkOperation.of(b -> b
                                .create(c -> c
                                        .id(UUID.randomUUID().toString())
                                        .document(doc))
                        )
                ).toList();
        try {
            BulkResponse response = elasticsearchClient.bulk(b -> b
                    .index(indexName)
                    .operations(bulkOperations)
            );
            return CompletableFuture.completedFuture(response);
        } catch (IOException e) {
            log.error("An exception occurred while indexing documents", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes all documents in a specified index where a given field has a particular value.
     *
     * @param indexName     the name of the index from which documents are to be deleted
     * @param fieldName     the name of the field to be checked in each document
     * @param fieldValue    the value of the field which, if matched, will result in the deletion of the document
     */
    public void deleteByFieldValue(String indexName, String fieldName, String fieldValue) {
        try {
            elasticsearchClient.deleteByQuery(d -> d
                    .index(indexName)
                    .query(q -> q
                            .term(t -> t
                                    .field(fieldName)
                                    .value(fieldValue)
                            )
                    )
            );
        } catch (IOException e) {
            log.error("An exception occurred during document removal", e);
            throw new RuntimeException(e);
        }
    }
}
