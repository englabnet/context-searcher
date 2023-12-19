package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.entities.IndexedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * This interface provides methods for querying indexed video objects
 * from the database. It extends JpaRepository for standard CRUD operations
 * on video entities.
 */
public interface IndexedVideoRepository extends JpaRepository<IndexedVideo, Long> {

    /**
     * Finds a video entity by its YouTube video ID.
     *
     * @param indexName         the name of the Elasticsearch index
     * @param youtubeVideoId    the YouTube video ID
     * @return  an Optional containing the found video.
     *          If no video is found, it will be empty.
     */
    Optional<IndexedVideo> findByIndexNameAndYoutubeVideoId(String indexName, String youtubeVideoId);
}
