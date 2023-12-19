package net.englab.contextsearcher.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.entities.IndexedVideo;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.repositories.IndexedVideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A storage service for indexed videos. It provides basic operations
 * such as saving, deleting, and searching subtitles.
 */
@Service
@RequiredArgsConstructor
public class IndexedVideoStorage {

    private final IndexedVideoRepository indexedVideoRepository;

    /**
     * Saves a new video to the storage.
     *
     * @param indexedVideo the video that needs to be saved
     */
    public void save(IndexedVideo indexedVideo) {
        indexedVideoRepository.save(indexedVideo);
    }

    /**
     * Deletes a video by its YouTube video ID.
     *
     * @param indexName         the name of the index
     * @param youtubeVideoId    the YouTube video ID
     */
    @Transactional
    public void delete(String indexName, String youtubeVideoId) {
        indexedVideoRepository.deleteByIndexNameAndYoutubeVideoId(indexName, youtubeVideoId);
    }

    /**
     * Finds subtitles by their YouTube video IDs.
     *
     * @param indexName         the name of the Elasticsearch index
     * @param youtubeVideoIds   a list of the YouTube video IDs
     * @return  a map where the key is a YouTube video ID
     *          and the value is a list of corresponding subtitles
     */
    public Map<String, List<SubtitleEntry>> findSubtitles(String indexName, Set<String> youtubeVideoIds) {
        return indexedVideoRepository.findByIndexNameAndYoutubeVideoIdIn(indexName, youtubeVideoIds).stream()
                .collect(Collectors.toMap(IndexedVideo::getYoutubeVideoId, IndexedVideo::getSubtitles));
    }

    /**
     * Removes stale videos that are left from previous indexations.
     *
     * @param indexName the current index name
     */
    @Transactional
    public void cleanUp(String indexName) {
        indexedVideoRepository.deleteAllByIndexNameIsNot(indexName);
    }
}
