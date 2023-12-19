package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.exceptions.ElasticOperationException;
import net.englab.contextsearcher.models.elastic.VideoFragmentDocument;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.search.VideoFragmentPage;
import net.englab.contextsearcher.models.search.VideoFragment;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.subtitles.SubtitleHighlighter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.VideoIndexProperties.*;

/**
 * A service that handles video searching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoSearcher {

    private final ElasticsearchClient elasticsearchClient;
    private final IndexedVideoStorage indexedVideoStorage;

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
        var hits = searchResponse.hits().hits();

        Set<String> youtubeVideoIds = hits.stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(VideoFragmentDocument::getYoutubeVideoId)
                .collect(Collectors.toSet());

        String indexName = hits.stream()
                .findAny()
                .map(Hit::index)
                .orElse("");

        Map<String, List<SubtitleEntry>> subtitleMap = indexedVideoStorage.findSubtitles(indexName, youtubeVideoIds);

        List<VideoFragment> videos = hits.stream()
                .map(hit -> buildVideoFragment(hit, subtitleMap))
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
                    .index(VIDEO_INDEX_NAME)
                    .from(from)
                    .size(size)
                    .query(buildSearchQuery(phrase, variety)._toQuery())
                    .highlight(h -> h
                            .fields(SENTENCE, f -> f
                                    .numberOfFragments(0)
                            )
                    ), VideoFragmentDocument.class);
        } catch (IOException e) {
            throw new ElasticOperationException("An exception occurred during video search", e);
        }
    }

    private static BoolQuery buildSearchQuery(String phrase, EnglishVariety variety) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
                .must(m -> m.matchPhrase(p -> p.field(SENTENCE).query(phrase)));
        if (variety != EnglishVariety.ALL) {
            builder.filter(f -> f.term(t -> t.field(ENGLISH_VARIETY).value(variety.name())));
        }
        return builder.build();
    }

    /**
     * Builds a VideoFragment based on the Elasticsearch response.
     */
    private VideoFragment buildVideoFragment(Hit<VideoFragmentDocument> hit, Map<String, List<SubtitleEntry>> subtitleMap) {
        VideoFragmentDocument doc = hit.source();
        if (doc == null) {
            throw new IllegalStateException("Video fragment cannot be null");
        }

        RangeMap<Integer, Integer> sentenceRangeMap = doc.getSentenceRangeMap();

        Integer firstEntryIndex = sentenceRangeMap.get(0);
        Integer lastEntryIndex = sentenceRangeMap.get(doc.getSentence().length() - 1);

        if (firstEntryIndex == null || lastEntryIndex == null) {
            throw new IllegalStateException(
                    "Failed to find the first and last subtitle entries. Sentence range map is not correct."
            );
        }

        List<SubtitleEntry> subtitles = subtitleMap.get(doc.getYoutubeVideoId());

        // here, we find all the subtitle entries that should contain our highlighted phrase
        // this small trick will boost performance since we don't need to go through all the subtitles
        List<SubtitleEntry> relevantSubtitleEntries = subtitles.subList(firstEntryIndex, lastEntryIndex + 1);

        // elastic wraps highlighted text in <em> and </em> tags
        String highlight = hit.highlight().get(SENTENCE).get(0);
        String[] textParts = highlight.split("<em>|</em>");

        int sentencePosition = doc.getSentencePosition();

        SubtitleHighlighter.highlight(textParts, sentencePosition, relevantSubtitleEntries);

        int firstHighlightPosition = textParts[0].length();
        Integer subtitleEntryIndex = sentenceRangeMap.get(firstHighlightPosition);

        if (subtitleEntryIndex == null) {
            throw new IllegalStateException("Failed to find the subtitle entry index. Sentence range map is not correct.");
        }

        return new VideoFragment(doc.getYoutubeVideoId(), subtitleEntryIndex, subtitles);
    }
}
