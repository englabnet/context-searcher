package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * This interface provides methods for querying video objects
 * from the database. It extends JpaRepository for standard CRUD operations and
 * JpaSpecificationExecutor for advanced queries on video entities.
 */
public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
}
