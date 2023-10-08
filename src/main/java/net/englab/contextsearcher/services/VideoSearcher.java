package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.dto.SubtitleBlock;
import net.englab.contextsearcher.models.dto.VideoSearchResponse;
import net.englab.contextsearcher.models.dto.VideoSearchResult;
import net.englab.contextsearcher.utils.SubtitleHighlighter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoSearcher {

    private final ElasticService elasticService;
    private final VideoStorage videoStorage;

    public VideoSearchResponse search(String phrase, EnglishVariety variety, int from, int size) {
        var searchResponse = elasticService.searchVideoByPhrase("videos", phrase, variety, from, size);

        List<VideoSearchResult> videos = searchResponse.hits().hits().stream()
                .map(this::buildSearchResponse)
                .toList();

        return new VideoSearchResponse(searchResponse.hits().total().value(), videos);
    }

    private VideoSearchResult buildSearchResponse(Hit<VideoDocument> hit) {
        VideoDocument doc = hit.source();

        List<SubtitleBlock> subtitles = videoStorage.findSubtitlesByVideoId(doc.getVideoId());

        String highlight = hit.highlight().get("sentence").get(0);
        String[] parts = highlight.split("<em>|</em>");
        RangeMap<Integer, Integer> ranges = mapToRanges(doc.getSubtitleBlocks());

        int beginIndex = ranges.get(0);
        int endIndex = ranges.get(doc.getSentence().length() - 1);
        SubtitleHighlighter.highlight(doc.getSentence(), parts, subtitles.subList(beginIndex, endIndex + 1));

        int index = ranges.get(parts[0].length());
        return new VideoSearchResult(doc.getVideoId(), index, subtitles);
    }

    private RangeMap<Integer, Integer> mapToRanges(Map<String, Integer> map) {
        RangeMap<Integer, Integer> ranges = TreeRangeMap.create();
        map.forEach((key, value) -> {
            String strRange = key.substring(1, key.length() - 1);
            String[] range = strRange.split("\\.\\.");
            ranges.put(Range.closed(Integer.valueOf(range[0]), Integer.valueOf(range[1])), value);
        });
        return ranges;
    }
}
