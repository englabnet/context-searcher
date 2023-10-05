package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.utils.SrtSentenceParser;
import net.englab.contextsearcher.utils.SrtSubtitles;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.ElasticProperties.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoIndexer {

    private final static String VIDEOS_INDEX = "videos";

    private final VideoStorage videoStorage;
    private final ElasticService elasticService;

    public void add(String videoId, EnglishVariety variety, String srt, boolean index) {
        Long id = videoStorage.save(new Video(null, videoId, variety, srt));
        if (!index) return;
        try {
            indexVideo(videoId, variety, srt);
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

    public void reindexAll() {
        elasticService.removeIndex(VIDEOS_INDEX);
        videoStorage.findAll().forEach(video ->
                indexVideo(video.getVideoId(), video.getVariety(), video.getSrt())
        );
    }

    private void indexVideo(String videoId, EnglishVariety variety, String srt) {
        SrtSubtitles subtitles = new SrtSubtitles(srt);
        List<SrtSentence> sentences = SrtSentenceParser.parse(subtitles);
        elasticService.createIndexIfAbsent(VIDEOS_INDEX, Map.of(
                "video_id", KEYWORD_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "variety", KEYWORD_PROPERTY,
                "subtitle_blocks", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));

        List<VideoDocument> docs = sentences.stream()
                .map(sentence -> {
                    RangeMap<Integer, Integer> ranges = sentence.subtitleBlocks();
                    return new VideoDocument(videoId, variety.name(), sentence.text(), rangesToMap(ranges));
                }).toList();
        elasticService.indexDocuments(VIDEOS_INDEX, docs);
    }

    public Map<String, Integer> rangesToMap(RangeMap<Integer, Integer> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
