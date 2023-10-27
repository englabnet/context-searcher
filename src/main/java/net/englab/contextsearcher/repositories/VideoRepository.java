package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
    Optional<Video> findByVideoId(String videoId);
    void deleteByVideoId(String videoId);
}
