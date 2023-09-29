package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.services.VideoIndexer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/indexer")
public class IndexingController {

    private final VideoIndexer videoIndexer;

    /**
     * Indexes the given video and its subtitles.
     *
     * @param videoId   the video id
     * @param variety   the variety of English used in the video
     * @param srt       the subtitles for the video in SRT format.
     * @return the status after attempting to index.
     */
    @PostMapping("/index")
    public String index(@RequestParam String videoId, @RequestParam EnglishVariety variety, @RequestBody String srt) {
        videoIndexer.index(videoId, variety, srt);
        return "index";
    }

    /**
     * Full reindex.
     *
     * @return the current status
     */
    @PostMapping("/reindex")
    public String reindex() {
        videoIndexer.reindexAll();
        return "reindex";
    }

    /**
     * Returns the list of indexed video links.
     *
     * @return a list of links
     */
    @GetMapping("/links")
    public List<String> links() {
        // todo: implement
        return List.of("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    }
}
