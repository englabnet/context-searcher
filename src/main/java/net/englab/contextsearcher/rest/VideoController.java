package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.exceptions.IndexingConflictException;
import net.englab.contextsearcher.exceptions.VideoAlreadyExistsException;
import net.englab.contextsearcher.exceptions.VideoNotFoundException;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.dto.VideoDto;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.services.VideoIndexer;
import net.englab.contextsearcher.services.VideoStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static net.englab.contextsearcher.repositories.VideoSpecifications.*;

/**
 * A REST controller that handles all the operations related to videos: getting, adding, modifying, removing.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoIndexer videoIndexer;
    private final VideoStorage videoStorage;

    /**
     * Returns a page of videos that match the specified filters.
     *
     * @param id        filter by ID
     * @param videoId   filter by YouTube video ID
     * @param variety   filter by variety of English
     * @param pageable  pagination and sorting
     * @return a page of videos
     */
    @GetMapping
    public Page<Video> getVideos(Long id, String videoId, EnglishVariety variety, Pageable pageable) {
        Specification<Video> specification = byId(id)
                .and(byVideoId(videoId))
                .and(byVariety(variety));
        return videoStorage.findAll(specification, pageable);
    }

    /**
     * Adds a new video.
     *
     * @param video the video that needs to be added
     * @return a status message after adding the video
     */
    @PostMapping
    public String add(@RequestBody VideoDto video) {
        try {
            videoIndexer.add(video.videoId(), video.variety(), video.srt());
            return "The video has been added";
        } catch (IndexingConflictException | VideoAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Updates the given video.
     *
     * @param id    the ID of the video that needs to be updated
     * @param video the modified video data
     * @return a status message after updating the video
     */
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody VideoDto video) {
        try {
            videoIndexer.update(id, video.videoId(), video.variety(), video.srt());
            return "The video has been updated";
        } catch (IndexingConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (VideoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Removes a video by the specified ID.
     *
     * @param id the ID of the video that needs to be removed
     * @return a status message after removing the video
     */
    @DeleteMapping("/{id}")
    public String remove(@PathVariable Long id) {
        try {
            videoIndexer.remove(id);
            return "The video has been removed";
        } catch (IndexingConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (VideoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
