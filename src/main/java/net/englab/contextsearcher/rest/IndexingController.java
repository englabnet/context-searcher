package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.EnglishVariety;
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
@RequestMapping("/api/v1/indexer")
public class IndexingController {

    private final VideoIndexer videoIndexer;
    private final VideoStorage videoStorage;

    /**
     * Adds the given video.
     *
     * @param videoId   the video id
     * @param variety   the variety of English used in the video
     * @param index     true if we want to add it to the index as well
     * @param srt       the subtitles for the video in SRT format
     * @return the status after adding
     */
    @PostMapping("/add")
    public String add(
            @RequestParam String videoId,
            @RequestParam EnglishVariety variety,
            @RequestParam boolean index,
            @RequestBody String srt) {
        videoIndexer.add(videoId, variety, srt, index);
        return "add";
    }

    /**
     * Removes the given video.
     *
     * @param videoId   the video id
     * @param index     true if we want to remove it from the index as well
     * @return the status after removing
     */
    @PostMapping("/remove")
    public String remove(@RequestParam String videoId, @RequestParam boolean index) {
        videoIndexer.remove(videoId, index);
        return "remove";
    }

    /**
     * Full indexing.
     *
     * @return the status after indexing
     */
    @PostMapping("/index")
    public String index() {
        videoIndexer.indexAll();
        return "reindex";
    }

    /**
     * Returns the list of videos we have in the system.
     *
     * @param id the id
     * @param videoId the video id
     * @param variety the english variety
     * @param pageable pagination and sorting
     * @return a list of videos
     */
    @GetMapping("/videos")
    public Page<Video> videos(Long id, String videoId, EnglishVariety variety, Pageable pageable) {
        Specification<Video> specification = byId(id)
                .and(byVideoId(videoId))
                .and(byVariety(variety));
        return videoStorage.findAll(specification, pageable);
    }
}
