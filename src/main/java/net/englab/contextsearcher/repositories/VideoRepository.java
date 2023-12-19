package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * This interface provides methods for querying video objects
 * from the database. It extends JpaRepository for standard CRUD operations and
 * JpaSpecificationExecutor for advanced queries on video entities.
 */
public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {

    /**
     * Finds a video entity by its YouTube video ID.
     *
     * @param youtubeVideoId the YouTube video ID
     * @return  an Optional containing the found video.
     *          If no video is found, it wil be empty.
     */
    Optional<Video> findByYoutubeVideoId(String youtubeVideoId);
}
