package net.englab.contextsearcher.services;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.repositories.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * A video storage service that provides basic operations
 * such as saving, deleting, and searching videos.
 */
@Service
@RequiredArgsConstructor
public class VideoStorage {

    private final VideoRepository videoRepository;

    /**
     * Saves a new video to the storage.
     *
     * @param video the video that needs to be saved
     * @return the unique ID assigned to the saved video
     */
    @Transactional
    public Long save(Video video) {
        return videoRepository.save(video).getId();
    }

    /**
     * Deletes a video by its unique identifier.
     *
     * @param id the unique ID of the video
     */
    @Transactional
    public void deleteById(Long id) {
        videoRepository.deleteById(id);
    }

    /**
     * Finds any videos that matched the specified filters.
     *
     * @param specification the specified filters
     * @return  an Optional containing the found video.
     *          If no video is found, it wil be empty.
     */
    @Transactional(readOnly = true)
    public Optional<Video> findAny(Specification<Video> specification) {
        return videoRepository.findOne(specification);
    }

    /**
     * Finds all videos.
     *
     * @return a list of all videos that we have in the storage
     */
    @Transactional(readOnly = true)
    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    /**
     * Finds all videos that match the specified filters.
     *
     * @param specification the specified filters
     * @param pageable      the pagination information
     * @return a page of videos
     */
    @Transactional(readOnly = true)
    public Page<Video> findAll(Specification<Video> specification, Pageable pageable) {
        return videoRepository.findAll(specification, pageable);
    }
}
