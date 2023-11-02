package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.IndexingInfo;
import net.englab.contextsearcher.services.VideoIndexer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/indexer")
public class IndexingController {

    private final VideoIndexer videoIndexer;

    /**
     * Full indexing.
     *
     * @return the status after indexing
     */
    @PostMapping("/index")
    public String index() {
        videoIndexer.startIndexing();
        return "Indexing has been started";
    }

    /**
     * Retrieves the current indexing status.
     *
     * @return the current indexing status
     */
    @GetMapping("/status")
    public IndexingInfo getStatus() {
        return videoIndexer.getIndexingStatus();
    }
}
