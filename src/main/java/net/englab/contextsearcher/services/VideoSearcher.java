package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.models.elastic.VideoFragmentDocument;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.models.search.VideoFragmentPage;
import net.englab.contextsearcher.models.search.VideoFragment;
import net.englab.contextsearcher.subtitles.SubtitleHighlighter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A service that handles video searching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoSearcher {

    private final static String VIDEOS_INDEX = "videos";

    private final ElasticsearchClient elasticsearchClient;
    private final VideoStorage videoStorage;

    /**
     * Finds video documents in the video index containing the given phrase.
     *
     * @param phrase    the phrase to search for
     * @param variety   the specific variety of English
     * @param from      the offset from the start of the results
     * @param size      the number of results to be returned
     * @return a page with video fragments
     */
    public VideoFragmentPage search(String phrase, EnglishVariety variety, int from, int size) {
        var searchResponse = searchDocuments(phrase, variety, from, size);

        List<VideoFragment> videos = searchResponse.hits().hits().stream()
                .map(this::buildVideoFragment)
                .toList();

        long totalElements = Optional.of(searchResponse.hits())
                .map(HitsMetadata::total)
                .map(TotalHits::value)
                .orElse(0L);

        return new VideoFragmentPage(totalElements, videos);
    }

    private SearchResponse<VideoFragmentDocument> searchDocuments(String phrase, EnglishVariety variety, int from, int size) {
        try {
            return elasticsearchClient.search(b -> b
                    .index(VIDEOS_INDEX)
                    .from(from)
                    .size(size)
                    .query(buildVideoQuery(phrase, variety)._toQuery())
                    .highlight(h -> h
                            .fields("sentence", f -> f
                                    .numberOfFragments(0)
                            )
                    ), VideoFragmentDocument.class);
        } catch (IOException e) {
            log.error("An exception occurred during video search", e);
            throw new RuntimeException(e);
        }
    }

    private static BoolQuery buildVideoQuery(String phrase, EnglishVariety variety) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
                .must(m -> m.matchPhrase(p -> p.field("sentence").query(phrase)));
        if (variety != EnglishVariety.ALL) {
            builder.filter(f -> f.term(t -> t.field("variety").value(variety.name())));
        }
        return builder.build();
    }

    /**
     * Builds a VideoFragments based on the Elasticsearch response.
     */
    private VideoFragment buildVideoFragment(Hit<VideoFragmentDocument> hit) {
        VideoFragmentDocument doc = hit.source();
        if (doc == null) {
            throw new IllegalStateException("Video fragment cannot be null");
        }

        List<SubtitleEntry> subtitles = videoStorage.findSubtitlesByVideoId(doc.getVideoId());

        String highlight = hit.highlight().get("sentence").get(0);

        // elastic wraps highlighted text in <em> and </em> tags
        String[] textParts = highlight.split("<em>|</em>");
        RangeMap<Integer, Integer> sentenceRangeMap = mapToRanges(doc.getSentenceRangeMap());

        Integer firstEntryIndex = sentenceRangeMap.get(0);
        Integer lastEntryIndex = sentenceRangeMap.get(doc.getSentence().length() - 1);

        if (firstEntryIndex == null || lastEntryIndex == null) {
            throw new IllegalStateException(
                    "Failed to find the first and last subtitle entries. Sentence range map is not correct."
            );
        }

        // here, we find all the subtitle entries that contain our sentence
        List<SubtitleEntry> relevantSubtitleEntries = subtitles.subList(firstEntryIndex, lastEntryIndex + 1);

        SubtitleHighlighter.highlight(doc.getSentence(), textParts, relevantSubtitleEntries);

        int firstHighlightPosition = textParts[0].length();
        Integer subtitleEntryIndex = sentenceRangeMap.get(firstHighlightPosition);

        if (subtitleEntryIndex == null) {
            throw new IllegalStateException("Failed to find the subtitle entry index. Sentence range map is not correct.");
        }

        return new VideoFragment(doc.getVideoId(), subtitleEntryIndex, subtitles);
    }

    /**
     * Converts a range map from Elasticsearch to a guava range map.
     */
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
