package net.englab.contextsearcher.services;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.entities.IndexedVideo;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import net.englab.contextsearcher.repositories.IndexedVideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A storage service for indexed videos. It provides basic operations
 * such as saving, deleting, and searching subtitles.
 */
@Service
@RequiredArgsConstructor
public class IndexedVideoStorage {

    private final IndexedVideoRepository indexedVideoRepository;

    public void save(IndexedVideo indexedVideo) {
        indexedVideoRepository.save(indexedVideo);
    }

    /**
     * Finds subtitles by YouTube video ID.
     *
     * @param indexName         the name of the Elasticsearch index
     * @param youtubeVideoId    the YouTube video ID
     * @return a list of subtitle entries
     */
    public List<SubtitleEntry> findSubtitlesByVideoId(String indexName, String youtubeVideoId) {
        return indexedVideoRepository.findByIndexNameAndYoutubeVideoId(indexName, youtubeVideoId)
                .map(IndexedVideo::getSubtitles)
                .orElse(List.of());
    }
}
