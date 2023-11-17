package net.englab.contextsearcher.rest;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.search.VideoSearchResult;
import net.englab.contextsearcher.services.VideoSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A REST controller that provides the search functionality.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final VideoSearcher videoSearcher;

    /**
     * Searches for video fragments in which the given phrase is mentioned.
     *
     * @param phrase    the search phrase
     * @param variety   filter by variety of English
     * @param from      the offset which determines where the page begins
     * @param size      the size of the page
     * @return a video search result
     */
    @GetMapping
    public VideoSearchResult search(
            String phrase,
            EnglishVariety variety,
            @RequestParam(defaultValue = "0") int from, // TODO: change it to a page
            @RequestParam(defaultValue = "10") int size) {
        return videoSearcher.search(phrase, variety, from, size);
    }
}
