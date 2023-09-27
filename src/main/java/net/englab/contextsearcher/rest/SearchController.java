package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.dto.VideoSearchResponse;
import net.englab.contextsearcher.services.VideoSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/searcher")
public class SearchController {

    private final VideoSearcher videoSearcher;

    @GetMapping("/search")
    public VideoSearchResponse search(
            String phrase,
            EnglishVariety variety,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return videoSearcher.search(phrase, variety, from, size);
    }
}
