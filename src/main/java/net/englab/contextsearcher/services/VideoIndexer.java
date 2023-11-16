package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.exceptions.IndexingConflictException;
import net.englab.contextsearcher.exceptions.VideoAlreadyExistsException;
import net.englab.contextsearcher.exceptions.VideoNotFoundException;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.elastic.IndexMetadata;
import net.englab.contextsearcher.models.IndexingInfo;
import net.englab.contextsearcher.models.subtitles.SubtitleSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.subtitles.SubtitleSentenceExtractor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.ElasticProperties.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoIndexer {

    private final static int BULK_SIZE = 10_000;
    public final static String VIDEOS_INDEX = "videos";

    private final VideoStorage videoStorage;
    private final ElasticService elasticService;

    private final ThreadPoolTaskExecutor executor;
    private IndexingInfo indexingInfo = IndexingInfo.none();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Adds a new video.
     *
     * @param videoId   the video id
     * @param variety   the variety of English used in the video
     * @param srt       the subtitles for the video in SRT format
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoAlreadyExistsException if the video already exists
     */
    public void add(String videoId, EnglishVariety variety, String srt) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be indexed while an indexing job is running");
        }
        videoStorage.findByVideoId(videoId).ifPresent(video -> {
            throw new VideoAlreadyExistsException("The video already exists.");
        });
        Video video = new Video(null, videoId, variety, srt);
        Long id = videoStorage.save(video);
        try {
            indexVideos(VIDEOS_INDEX, List.of(video));
        } catch (Exception e) {
            log.error("Exception occurred during video indexing", e);
            videoStorage.deleteById(id);
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the specified video.
     *
     * @param id        the id
     * @param videoId   the video id
     * @param variety   the variety of English used in the video
     * @param srt       the subtitles for the video in SRT format
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoNotFoundException if the video is not found
     */
    public void update(Long id, String videoId, EnglishVariety variety, String srt) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be updated while an indexing job is running");
        }
        videoStorage.findById(id).ifPresentOrElse(video -> {
            video.setVideoId(videoId);
            video.setVariety(variety);
            video.setSrt(srt);
            videoStorage.save(video);
            elasticService.removeVideo(VIDEOS_INDEX, video.getVideoId());
            try {
                indexVideos(VIDEOS_INDEX, List.of(video));
            } catch (Exception e) {
                log.error("Exception occurred during video updating", e);
                videoStorage.deleteById(id);
                throw new RuntimeException(e);
            }
        }, () -> {
            throw new VideoNotFoundException("The video has not been found and cannot be modified.");
        });
    }

    /**
     * Removes a video by the specified id.
     *
     * @param id the id
     * @throws IndexingConflictException if an indexing job is running
     * @throws VideoNotFoundException if the video is not found
     */
    public void remove(Long id) {
        if (isRunning.get()) {
            throw new IndexingConflictException("A video cannot be removed while an indexing job is running");
        }
        try {
            videoStorage.findById(id).ifPresentOrElse(video ->
                    elasticService.removeVideo(VIDEOS_INDEX, video.getVideoId()),
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
            elasticService.getIndexMetadata(VIDEOS_INDEX)
                    .ifPresent(meta -> indexingInfo = IndexingInfo.completed(meta.startTime(), meta.finishTime()));
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
                elasticService.removeIndex(VIDEOS_INDEX);
                log.info("The old index has been removed.");
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

        Optional<String> oldIndexName = elasticService.getIndexName(VIDEOS_INDEX);

        String indexName = generateVideoIndexName();
        elasticService.createIndex(indexName, Map.of(
                "video_id", KEYWORD_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "variety", KEYWORD_PROPERTY,
                "subtitle_blocks", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));
        log.info("A new index '{}' has been created.", indexName);

        indexVideos(indexName, videos);

        elasticService.setIndexMetadata(indexName, new IndexMetadata(startTime, Instant.now()));
        log.info("The index metadata has been updated.");

        elasticService.putAlias(indexName, VIDEOS_INDEX);
        log.info("The alias has been updated.");

        oldIndexName.ifPresent(elasticService::removeIndex);
        log.info("The old index has been removed.");
    }

    @SneakyThrows
    private void indexVideos(String indexName, Collection<Video> videos) {
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
        return VIDEOS_INDEX + "_" + Instant.now().toEpochMilli();
    }

    private List<Future<BulkResponse>> bulkIndex(String index, Collection<Video> videos) {
        List<VideoDocument> docs = new ArrayList<>();
        List<Future<BulkResponse>> futures = new ArrayList<>();
        for (Video video : videos) {
            List<SubtitleSentence> sentences = SubtitleSentenceExtractor.extract(video.getSrt());
            for (SubtitleSentence sentence : sentences) {
                VideoDocument doc = new VideoDocument(
                        video.getVideoId(),
                        video.getVariety().name(),
                        sentence.text(),
                        rangesToMap(sentence.subtitleBlocks()));
                if (docs.size() >= BULK_SIZE) {
                    futures.add(elasticService.indexDocuments(index, docs));
                    docs = new ArrayList<>();
                }
                docs.add(doc);
            }
        }
        if (!docs.isEmpty()) {
            futures.add(elasticService.indexDocuments(index, docs));
        }
        return futures;
    }

    private static Map<String, Integer> rangesToMap(RangeMap<Integer, Integer> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
