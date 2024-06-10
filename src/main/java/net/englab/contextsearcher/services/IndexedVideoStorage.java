package net.englab.contextsearcher.services;

import lombok.RequiredArgsConstructor;
import net.englab.common.search.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.models.entities.IndexedVideo;
import net.englab.contextsearcher.repositories.IndexedVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A storage service for indexed videos.
 */
@Service
@RequiredArgsConstructor
public class IndexedVideoStorage {

    private final IndexedVideoRepository indexedVideoRepository;

    /**
     * Finds subtitles by their YouTube video IDs.
     *
     * @param indexName         the name of the Elasticsearch index
     * @param youtubeVideoIds   a list of the YouTube video IDs
     * @return  a map where the key is a YouTube video ID
     *          and the value is a list of corresponding subtitles
     */
    @Transactional(readOnly = true)
    public Map<String, List<SubtitleEntry>> findSubtitles(String indexName, Set<String> youtubeVideoIds) {
        return indexedVideoRepository.findByIndexNameAndYoutubeVideoIdIn(indexName, youtubeVideoIds).stream()
                .collect(Collectors.toMap(IndexedVideo::getYoutubeVideoId, IndexedVideo::getSubtitles));
    }
}
