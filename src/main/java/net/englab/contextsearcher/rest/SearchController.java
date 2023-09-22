package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.model.VideoSearchResult;
import net.englab.contextsearcher.service.VideoSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/searcher")
public class SearchController {

    private final VideoSearcher videoSearcher;

    @GetMapping("/search")
    public List<VideoSearchResult> search(String phrase) {
        return videoSearcher.search(phrase);
    }
}
