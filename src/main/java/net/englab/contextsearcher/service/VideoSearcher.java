package net.englab.contextsearcher.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.model.EnglishVariety;
import net.englab.contextsearcher.model.TimeFrame;
import net.englab.contextsearcher.model.VideoSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoSearcher {

    private final ElasticService elasticService;

    public List<VideoSearchResult> search(String phrase, EnglishVariety variety) {
        var searchResponse = elasticService.searchVideoByPhrase("videos", phrase, variety);

        return searchResponse.hits().hits().stream()
                .map(this::buildSearchResponse)
                .collect(Collectors.toList());
    }

    private VideoSearchResult buildSearchResponse(Hit<VideoDocument> hit) {
        VideoDocument doc = hit.source();

        String highlight = hit.highlight().get("sentence").get(0);
        String[] parts = highlight.split("<em>|</em>");

        int startIndex = parts[0].length();
        int endIndex = doc.getSentence().length() - parts[parts.length - 1].length() - 1;

        RangeMap<Integer, TimeFrame> ranges = mapToRanges(doc.getTimeRanges());
        long startTimeInSec = ranges.get(startIndex).startTime();
        long endTimeInSec = ranges.get(endIndex).endTime();
        TimeFrame timeFrame = new TimeFrame(startTimeInSec, endTimeInSec);
        return new VideoSearchResult(doc.getVideoId(), timeFrame, doc.getSentence());
    }

    private RangeMap<Integer, TimeFrame> mapToRanges(Map<String, TimeFrame> map) {
        RangeMap<Integer, TimeFrame> ranges = TreeRangeMap.create();
        map.forEach((key, value) -> {
            String strRange = key.substring(1, key.length() - 1);
            String[] range = strRange.split("\\.\\.");
            ranges.put(Range.closed(Integer.valueOf(range[0]), Integer.valueOf(range[1])), value);
        });
        return ranges;
    }
}
