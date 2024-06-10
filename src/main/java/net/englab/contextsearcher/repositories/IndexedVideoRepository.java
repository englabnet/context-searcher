package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.entities.IndexedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

/**
 * This interface provides methods for querying indexed video objects
 * from the database. It extends JpaRepository for standard CRUD operations
 * on video entities.
 */
public interface IndexedVideoRepository extends JpaRepository<IndexedVideo, Long> {

    /**
     * Finds a list of indexed videos by their YouTube video IDs.
     *
     * @param indexName         the name of the Elasticsearch index
     * @param youtubeVideoIds   a set of the YouTube video IDs
     * @return  an Optional containing the found video.
     *          If no video is found, it will be empty.
     */
    List<IndexedVideo> findByIndexNameAndYoutubeVideoIdIn(String indexName, Set<String> youtubeVideoIds);
}
