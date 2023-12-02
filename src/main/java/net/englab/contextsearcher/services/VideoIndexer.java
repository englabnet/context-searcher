package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.models.elastic.VideoFragmentDocument;
import net.englab.contextsearcher.exceptions.IndexingConflictException;
import net.englab.contextsearcher.exceptions.VideoAlreadyExistsException;
import net.englab.contextsearcher.exceptions.VideoNotFoundException;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.elastic.VideoIndexMetadata;
import net.englab.contextsearcher.models.indexing.IndexingInfo;
import net.englab.contextsearcher.models.subtitles.SubtitleSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.services.elastic.ElasticDocumentManager;
import net.englab.contextsearcher.services.elastic.ElasticIndexManager;
import net.englab.contextsearcher.subtitles.SubtitleSentenceExtractor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.englab.contextsearcher.elastic.VideoIndexProperties.*;
import static net.englab.contextsearcher.repositories.VideoSpecifications.*;

/**
 * A video indexer service that allows us to index, update, and remove videos one-by-one
 * as well as reindex the full dataset entirely by starting an indexing job.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoIndexer {
    private static final int BULK_SIZE = 10_000;
    private static final Map<String, Property> VIDEO_INDEX_PROPERTIES = Map.of(
            YOUTUBE_VIDEO_ID, KeywordProperty.of(b -> b)._toProperty(),
            SENTENCE, TextProperty.of(b -> b)._toProperty(),
            ENGLISH_VARIETY, KeywordProperty.of(b -> b)._toProperty(),
            SUBTITLE_RANGE_MAP, ObjectProperty.of(b -> b.enabled(false))._toProperty()
    );

    private final VideoStorage videoStorage;

    private final ElasticIndexManager indexManager;
    private final ElasticDocumentManager documentManager;
    private final SubtitleSentenceExtractor sentenceExtractor = new SubtitleSentenceExtractor();

    private final ThreadPoolTaskExecutor executor;
    private IndexingInfo indexingInfo = IndexingInfo.none();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Adds a new video.
     *
     * @param videoId   the YouTube video ID
     * @param variety   the variety of English used in the video
     * @param srt       the subtitles for the video in SRT format
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoAlreadyExistsException if the video already exists
     */
    public void add(String videoId, EnglishVariety variety, String srt) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be indexed while an indexing job is running");
        }
        videoStorage.findAny(byVideoId(videoId)).ifPresent(video -> {
            throw new VideoAlreadyExistsException("The video already exists.");
        });
        Video video = new Video(null, videoId, variety, srt);
        Long id = videoStorage.save(video);
        log.info("A new video with ID={} has been added", id);
        try {
            indexVideos(VIDEO_INDEX_NAME, List.of(video));
        } catch (Exception e) {
            log.error("Exception occurred during video indexing", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the specified video.
     *
     * @param id        the ID of the video we are updating
     * @param videoId   the updated YouTube video ID
     * @param variety   the updated variety of English used in the video
     * @param srt       the updated subtitles for the video in SRT format
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoNotFoundException if the video is not found
     */
    public void update(Long id, String videoId, EnglishVariety variety, String srt) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be updated while an indexing job is running");
        }
        videoStorage.findAny(byId(id)).ifPresentOrElse(video -> {
            video.setVideoId(videoId);
            video.setVariety(variety);
            video.setSrt(srt);
            videoStorage.save(video);
            documentManager.deleteByFieldValue(VIDEO_INDEX_NAME, YOUTUBE_VIDEO_ID, video.getVideoId());
            try {
                indexVideos(VIDEO_INDEX_NAME, List.of(video));
            } catch (Exception e) {
                log.error("Exception occurred during video updating", e);
                throw new RuntimeException(e);
            }
        }, () -> {
            throw new VideoNotFoundException("The video has not been found and cannot be modified.");
        });
    }

    /**
     * Removes a video by the specified ID.
     *
     * @param id the ID of the video we want to remove
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoNotFoundException if the video is not found
     */
    public void remove(Long id) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be removed while an indexing job is running");
        }
        try {
            videoStorage.findAny(byId(id)).ifPresentOrElse(video ->
                    documentManager.deleteByFieldValue(VIDEO_INDEX_NAME, YOUTUBE_VIDEO_ID, video.getVideoId()),
            () -> {
                throw new VideoNotFoundException("The video has not been found and cannot be removed.");
            });
        } catch (Exception e) {
            log.error("Exception occurred during video removal", e);
            throw new RuntimeException(e);
        }
        videoStorage.deleteById(id);
    }

    /**
     * Returns the current status of indexing.
     *
     * @return indexing info
     */
    public IndexingInfo getIndexingStatus() {
        if (!isRunning.get()) {
            Map<String, JsonData> metadata = indexManager.getMetadata(VIDEO_INDEX_NAME);
            if (!metadata.isEmpty()) {
                VideoIndexMetadata videoIndexMetadata = new VideoIndexMetadata(metadata);
                indexingInfo = IndexingInfo.completed(videoIndexMetadata.startTime(), videoIndexMetadata.finishTime());
            }
        }
        return indexingInfo;
    }

    /**
     * Starts a new indexing job.
     *
     * @throws IndexingConflictException if an indexing job has been already started
     */
    public void startIndexing() {
        if (isRunning.getAndSet(true)) {
            throw new IndexingConflictException("A new indexing job cannot be started if one is already running");
        }
        indexingInfo = IndexingInfo.started(Instant.now());
        executor.execute(() -> {
            try {
                log.info("Full indexing has been started.");
                log.info("Start reading videos from the database...");
                List<Video> videos = videoStorage.findAll();
                log.info("Start indexing the videos...");
                startFullIndexing(videos);
                log.info("Indexing has been finished successfully.");
            } catch (Throwable throwable) {
                indexingInfo = IndexingInfo.failed(indexingInfo.startTime(), Instant.now(), throwable.getMessage());
                log.error("An exception occurred during indexing", throwable);
                throw new RuntimeException(throwable);
            } finally {
                isRunning.set(false);
            }
        });
    }

    @SneakyThrows
    private void startFullIndexing(Collection<Video> videos) {
        Instant startTime = Instant.now();

        Optional<String> oldIndexName = indexManager.getIndexName(VIDEO_INDEX_NAME);

        String indexName = generateVideoIndexName();
        indexManager.create(indexName, VIDEO_INDEX_PROPERTIES);
        log.info("A new index '{}' has been created.", indexName);

        indexVideos(indexName, videos);

        VideoIndexMetadata videoIndexMetadata = new VideoIndexMetadata(startTime, Instant.now());
        indexManager.setMetadata(indexName, videoIndexMetadata.toMetadata());
        log.info("The index metadata has been updated.");

        indexManager.putAlias(indexName, VIDEO_INDEX_NAME);
        log.info("The alias has been updated.");

        oldIndexName.ifPresent(indexManager::delete);
        log.info("The old index has been removed.");
    }

    @SneakyThrows
    private void indexVideos(String indexName, Collection<Video> videos) {
        if (!indexManager.exists(VIDEO_INDEX_NAME)) {
            return;
        }
        List<Future<BulkResponse>> futures = bulkIndex(indexName, videos);
        for (Future<BulkResponse> future : futures) {
            BulkResponse response = future.get();
            if (response.errors()) {
                throw new RuntimeException("Error occurred during video indexing: " + response);
            } else {
                log.info("{} docs have been successfully indexed. It took {} ms.", response.items().size(), response.took());
            }
        }
    }

    private static String generateVideoIndexName() {
        return VIDEO_INDEX_NAME + "_" + Instant.now().toEpochMilli();
    }

    private List<Future<BulkResponse>> bulkIndex(String index, Collection<Video> videos) {
        List<VideoFragmentDocument> docs = new ArrayList<>();
        List<Future<BulkResponse>> futures = new ArrayList<>();
        for (Video video : videos) {
            List<SubtitleSentence> sentences = sentenceExtractor.extract(video.getSrt());
            for (SubtitleSentence sentence : sentences) {
                VideoFragmentDocument doc = new VideoFragmentDocument(
                        video.getVideoId(),
                        video.getVariety(),
                        sentence.text(),
                        sentence.subtitleRangeMap()
                );
                if (docs.size() >= BULK_SIZE) {
                    futures.add(documentManager.index(index, docs));
                    docs = new ArrayList<>();
                }
                docs.add(doc);
            }
        }
        if (!docs.isEmpty()) {
            futures.add(documentManager.index(index, docs));
        }
        return futures;
    }
}
