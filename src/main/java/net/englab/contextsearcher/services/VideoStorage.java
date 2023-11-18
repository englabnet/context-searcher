package net.englab.contextsearcher.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.repositories.VideoRepository;
import net.englab.contextsearcher.subtitles.SrtSubtitles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * A video storage service that provides basic operations such as saving, deleting, and searching videos.
 */
@Service
@RequiredArgsConstructor
public class VideoStorage {

    private final VideoRepository videoRepository;

    /**
     * Saves a new video to the database.
     *
     * @param video the video that needs to be saved
     * @return the unique ID assigned to the saved video
     */
    public Long save(Video video) {
        return videoRepository.save(video).getId();
    }

    /**
     * Finds a video by its unique identifier.
     *
     * @param id the unique ID of the video
     * @return an Optional containing the found video. If no video is found, it wil be empty.
     */
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    /**
     * Finds a video by its YouTube video ID.
     *
     * @param videoId the YouTube video ID
     * @return an Optional containing the found video. If no video is found, it wil be empty.
     */
    public Optional<Video> findByVideoId(String videoId) {
        return videoRepository.findByVideoId(videoId);
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
     * Finds all videos.
     *
     * @return a list of all videos that we have in the storage
     */
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
    public Page<Video> findAll(Specification<Video> specification, Pageable pageable) {
        return videoRepository.findAll(specification, pageable);
    }

    /**
     * Finds subtitles by YouTube video ID.
     *
     * @param videoId the YouTube video ID
     * @return a list of subtitle entries
     */
    public List<SubtitleEntry> findSubtitlesByVideoId(String videoId) {
        // TODO: These stream transformations make the search two times slower.
        //  I can optimise it by making it part of the indexing.
        return videoRepository.findByVideoId(videoId).stream()
                .map(Video::getSrt)
                .map(SrtSubtitles::new)
                .flatMap(SrtSubtitles::stream)
                .map(b -> new SubtitleEntry(
                        b.timeFrame().startTime(),
                        b.timeFrame().endTime(),
                        List.of(String.join(" ", b.text())))
                ).toList();
    }
}
