package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.utils.SrtSentenceParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.ElasticProperties.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoIndexer {

    private final static int BULK_SIZE = 10_000;
    private final static String VIDEOS_INDEX = "videos";

    private final VideoStorage videoStorage;
    private final ElasticService elasticService;

    public void add(String videoId, EnglishVariety variety, String srt, boolean index) {
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

    public void remove(String videoId, boolean index) {
        if (index) {
            try {
                elasticService.removeVideo(VIDEOS_INDEX, videoId);
            } catch (Exception e) {
                log.error("Exception occurred during video removal", e);
                throw new RuntimeException(e);
            }
        }
        videoStorage.deleteByVideoId(videoId);
    }

    public void indexAll() {
        log.info("Full indexing has been started.");
        elasticService.removeIndex(VIDEOS_INDEX);
        log.info("The old index has been removed.");
        indexVideos(videoStorage.findAll());
    }

    @SneakyThrows
    private void indexVideos(Collection<Video> videos) {
        elasticService.createIndexIfAbsent(VIDEOS_INDEX, Map.of(
                "video_id", KEYWORD_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "variety", KEYWORD_PROPERTY,
                "subtitle_blocks", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));
        List<Future<BulkResponse>> futures = bulkIndex(videos);
        for (Future<BulkResponse> future : futures) {
            BulkResponse response = future.get();
            if (response.errors()) {
                throw new RuntimeException("Error occurred during video indexing: " + response);
            } else {
                log.info("{} docs have been successfully indexed. It took {}ms.", response.items().size(), response.took());
            }
        }
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

    public Map<String, Integer> rangesToMap(RangeMap<Integer, Integer> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
