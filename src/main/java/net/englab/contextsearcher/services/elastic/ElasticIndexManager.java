package net.englab.contextsearcher.services.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.exceptions.ElasticOperationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Provides operations that are related to Elasticsearch indices
 * such as creation, deletion, setting aliases, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticIndexManager {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * Checks if the given index exists.
     *
     * @param indexName the name of the index to check
     * @return true if the index is present and false otherwise
     */
    public boolean exists(String indexName) {
        try {
            var response = elasticsearchClient.indices()
                    .exists(b -> b.index(indexName));
            return response.value();
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred while checking the existence of an index", e);
        }
    }

    /**
     * Creates a new index with the given name.
     *
     * @param indexName     the name of the index to create
     * @param properties    the mapping properties
     */
    public void create(String indexName, Map<String, Property> properties) {
        try {
            if (!exists(indexName)) {
                elasticsearchClient.indices()
                        .create(b -> b
                                .index(indexName)
                                .mappings(m -> m
                                        .properties(properties)
                                        .dynamic(DynamicMapping.Strict)
                                )
                        );
            }
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred during index creation", e);
        }
    }

    /**
     * Deletes the index with the given name.
     *
     * @param indexName the name of the index to delete
     */
    public void delete(String indexName) {
        try {
            elasticsearchClient.indices().delete(d -> d
                    .index(indexName)
                    .ignoreUnavailable(true)
            );
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred during index deletion", e);
        }
    }

    /**
     * Retrieves the metadata of the specified index.
     *
     * @param indexName the name of the index
     * @return  a map containing the index metadata. If index does not exist
     *          or does not have any metadata, the map will be empty.
     */
    public Map<String, JsonData> getMetadata(String indexName) {
        try {
            if (!exists(indexName)) {
                return Map.of();
            }
            var response = elasticsearchClient.indices()
                    .getMapping(m -> m.index(indexName));
            return Optional.of(response)
                    .map(this::findAnyIndexMapping)
                    .map(IndexMappingRecord::mappings)
                    .map(TypeMapping::meta)
                    .orElse(Map.of());
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred while getting metadata", e);
        }
    }

    private IndexMappingRecord findAnyIndexMapping(GetMappingResponse response) {
        return response.result().values().stream()
                .findAny()
                .orElse(null);
    }

    /**
     * Sets the index metadata for a given index.
     *
     * @param index     the index name
     * @param metadata  the index metadata
     */
    public void setMetadata(String index, Map<String, JsonData> metadata) {
        try {
            elasticsearchClient.indices().putMapping(m -> m
                    .index(index)
                    .meta(metadata)
            );
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred while setting metadata", e);
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
            throw new ElasticOperationException("An exception occurred while setting an alias", e);
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
            if (!exists(alias)) {
                return Optional.empty();
            }
            var response = elasticsearchClient.indices().get(b -> b.index(alias));
            return response.result().keySet().stream()
                    .findAny();
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred while getting an index name", e);
        }
    }
}
