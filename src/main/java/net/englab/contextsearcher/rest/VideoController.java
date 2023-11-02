package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.dto.VideoDto;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.services.VideoIndexer;
import net.englab.contextsearcher.services.VideoStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import static net.englab.contextsearcher.models.entities.VideoSpecifications.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoIndexer videoIndexer;
    private final VideoStorage videoStorage;

    /**
     * Returns the list of videos we have in the system.
     *
     * @param id the id
     * @param videoId the video id
     * @param variety the english variety
     * @param pageable pagination and sorting
     * @return a list of videos
     */
    @GetMapping
    public Page<Video> getVideos(Long id, String videoId, EnglishVariety variety, Pageable pageable) {
        Specification<Video> specification = byId(id)
                .and(byVideoId(videoId))
                .and(byVariety(variety));
        return videoStorage.findAll(specification, pageable);
    }

    /**
     * Adds the given video.
     *
     * @param video the video to add
     * @return the status after adding
     */
    @PostMapping
    public String add(@RequestBody VideoDto video) {
        videoIndexer.add(video.videoId(), video.variety(), video.srt());
        return "The video has been added";
    }

    /**
     * Updates the given video.
     *
     * @param video the video to update
     * @return the status after adding
     */
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody VideoDto video) {
        videoIndexer.update(id, video.videoId(), video.variety(), video.srt());
        return "The video has been updated";
    }

    /**
     * Removes a video by the specified id.
     *
     * @param id        the id
     * @return the status after removing
     */
    @DeleteMapping("/{id}")
    public String remove(@PathVariable Long id) {
        videoIndexer.remove(id);
        return "The video has been removed";
    }
}
