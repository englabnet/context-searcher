package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.service.VideoIndexer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/indexer")
public class IndexingController {

    private final VideoIndexer videoIndexer;

    /**
     * Index the given video links.
     *
     * @param videoId video id
     * @param srt video subtitles
     * @return the current status
     */
    @PostMapping("/index")
    public String index(@RequestParam String videoId, @RequestBody String srt) {
        videoIndexer.indexVideo(videoId, srt);
        return "index";
    }

    /**
     * Full reindex.
     *
     * @return the current status
     */
    @PostMapping("/reindex")
    public String reindex() {
        // todo: implement
        return "reindex";
    }

    /**
     * Get the video links that were indexed.
     *
     * @return a list of links
     */
    @GetMapping("/links")
    public List<String> links() {
        // todo: implement
        return List.of("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    }
}
