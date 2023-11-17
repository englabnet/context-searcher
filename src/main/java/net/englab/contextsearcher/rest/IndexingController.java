package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.exceptions.IndexingConflictException;
import net.englab.contextsearcher.models.indexing.IndexingInfo;
import net.englab.contextsearcher.services.VideoIndexer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * A REST controller that allows us to start a new indexing job and get the indexing status.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/indexer")
public class IndexingController {

    private final VideoIndexer videoIndexer;

    /**
     * Starts a new indexing job.
     *
     * @return a status message after starting an indexing job
     */
    @PostMapping("/index")
    public String index() {
        try {
            videoIndexer.startIndexing();
            return "Indexing has been started";
        } catch (IndexingConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Retrieves the current indexing status.
     *
     * @return an indexing status
     */
    @GetMapping("/status")
    public IndexingInfo getStatus() {
        return videoIndexer.getIndexingStatus();
    }
}
