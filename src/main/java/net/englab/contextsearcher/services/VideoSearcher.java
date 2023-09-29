package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.SubtitleBlock;
import net.englab.contextsearcher.models.dto.VideoSearchResponse;
import net.englab.contextsearcher.models.dto.VideoSearchResult;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.utils.SrtParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoSearcher {

    private final ElasticService elasticService;
    private final VideoStorage videoStorage;

    public VideoSearchResponse search(String phrase, EnglishVariety variety, int from, int size) {
        var searchResponse = elasticService.searchVideoByPhrase("videos", phrase, variety, from, size);

        List<VideoSearchResult> videos = searchResponse.hits().hits().stream()
                .map(this::buildSearchResponse)
                .collect(Collectors.toList());

        return new VideoSearchResponse(searchResponse.hits().total().value(), videos);
    }

    private VideoSearchResult buildSearchResponse(Hit<VideoDocument> hit) {
        VideoDocument doc = hit.source();

        String highlight = hit.highlight().get("sentence").get(0);
        String[] parts = highlight.split("<em>|</em>");

        int startIndex = parts[0].length();

        RangeMap<Integer, Integer> ranges = mapToRanges(doc.getSubtitleBlocks());
        int index = ranges.get(startIndex);

        List<SubtitleBlock> subtitles = videoStorage.findByVideoId(doc.getVideoId())
                .map(Video::getSrt)
                .map(SrtParser::parseSubtitles)
                .orElse(null);

        calculateHighlighting(index, subtitles, parts);

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

    private void calculateHighlighting(int index, List<SubtitleBlock> subtitles, String[] parts) {
        int currentBlock = index;
        int currentPart = 1;
        for (; currentBlock < subtitles.size() && currentPart < parts.length; currentBlock++) {
            SubtitleBlock subtitleBlock = subtitles.get(currentBlock);
            String text = subtitleBlock.getText().get(0);
            List<String> result = new ArrayList<>();
            for (; currentPart < parts.length; currentPart += 2) {
                String p = parts[currentPart];
                int start = text.indexOf(p);
                if (start == -1) break;
                result.add(text.substring(0, start));
                result.add(p);
                text = text.substring(start + p.length());
            }
            result.add(text);
            subtitleBlock.setText(result);
        }
    }
}
