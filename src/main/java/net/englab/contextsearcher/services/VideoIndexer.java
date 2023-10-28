package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.elastic.IndexMetadata;
import net.englab.contextsearcher.models.IndexingInfo;
import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.utils.SrtSentenceParser;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
     * @param index     true if we also want to index the video
     */
    public void add(String videoId, EnglishVariety variety, String srt, boolean index) {
        if (index && isRunning.get()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A video cannot be indexed while an indexing job is running"
            );
        }
        Video video = new Video(null, videoId, variety, srt);
        Long id = videoStorage.save(video);
        if (!index) return;
        try {
            indexVideos(List.of(video));
        } catch (Exception e) {
            log.error("Exception occurred during video indexing", e);
            videoStorage.deleteById(id);
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the given video.
     *
     * @param videoId   the video id
     * @param index     true if we also want to remove the video from the index
     */
    public void remove(String videoId, boolean index) {
        if (index) {
            if (isRunning.get()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A video cannot be removed from the index while an indexing job is running"
                );
            }
            try {
                elasticService.removeVideo(VIDEOS_INDEX, videoId);
            } catch (Exception e) {
                log.error("Exception occurred during video removal", e);
                throw new RuntimeException(e);
            }
        }
        videoStorage.deleteByVideoId(videoId);
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
     */
    public void startIndexing() {
        if (isRunning.getAndSet(true)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A new indexing job cannot be started if one is already running"
            );
        };
        indexingInfo = IndexingInfo.started(Instant.now());
        executor.execute(() -> {
            try {
                log.info("Full indexing has been started.");
                elasticService.removeIndex(VIDEOS_INDEX);
                log.info("The old index has been removed.");
                log.info("Start reading videos from the database...");
                List<Video> videos = videoStorage.findAll();
                log.info("Start indexing the videos...");
                indexVideos(videos);
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
    private void indexVideos(Collection<Video> videos) {
        Instant startTime = Instant.now();
        elasticService.createIndexIfAbsent(VIDEOS_INDEX, Map.of(
                "video_id", KEYWORD_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "variety", KEYWORD_PROPERTY,
                "subtitle_blocks", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));
        log.info("A new index has been created.");
        List<Future<BulkResponse>> futures = bulkIndex(videos);
        for (Future<BulkResponse> future : futures) {
            BulkResponse response = future.get();
            if (response.errors()) {
                throw new RuntimeException("Error occurred during video indexing: " + response);
            } else {
                log.info("{} docs have been successfully indexed. It took {}ms.", response.items().size(), response.took());
            }
        }
        elasticService.setIndexMetadata(VIDEOS_INDEX, new IndexMetadata(startTime, Instant.now()));
        log.info("The index metadata has been updated.");
    }

    private List<Future<BulkResponse>> bulkIndex(Collection<Video> videos) {
        List<VideoDocument> docs = new ArrayList<>();
        List<Future<BulkResponse>> futures = new ArrayList<>();
        for (Video video : videos) {
            List<SrtSentence> sentences = SrtSentenceParser.parse(video.getSrt());
            for (SrtSentence sentence : sentences) {
                VideoDocument doc = new VideoDocument(
                        video.getVideoId(),
                        video.getVariety().name(),
                        sentence.text(),
                        rangesToMap(sentence.subtitleBlocks()));
                if (docs.size() >= BULK_SIZE) {
                    futures.add(elasticService.indexDocuments(VIDEOS_INDEX, docs));
                    docs = new ArrayList<>();
                }
                docs.add(doc);
            }
        }
        if (!docs.isEmpty()) {
            futures.add(elasticService.indexDocuments(VIDEOS_INDEX, docs));
        }
        return futures;
    }

    private static Map<String, Integer> rangesToMap(RangeMap<Integer, Integer> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
