package net.englab.contextsearcher.services;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.models.EnglishVariety;
import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.utils.SrtParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.ElasticProperties.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoIndexer {

    private final static String VIDEOS_INDEX = "videos";

    private final VideoStorage videoStorage;
    private final ElasticService elasticService;

    public void index(String videoId, EnglishVariety variety, String srt) {
        Long id = videoStorage.save(new Video(null, videoId, variety, srt));
        try {
            indexVideo(videoId, variety, srt);
        } catch (Exception e) {
            log.error("Exception occurred during video indexing", e);
            videoStorage.deleteById(id);
        }
    }

    public void reindexAll() {
        elasticService.removeIndex("videos");
        videoStorage.findAll().forEach(video ->
                indexVideo(video.getVideoId(), video.getVariety(), video.getSrt())
        );
    }

    private void indexVideo(String videoId, EnglishVariety variety, String srt) {
        List<SrtSentence> sentences = SrtParser.parseSentences(srt);
        elasticService.createIndexIfAbsent(VIDEOS_INDEX, Map.of(
                "video_id", NON_SEARCHABLE_TEXT_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "variety", KEYWORD_PROPERTY,
                "subtitle_blocks", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));

        sentences.forEach(sentence -> {
            String docId = UUID.randomUUID().toString();
            RangeMap<Integer, Integer> ranges = sentence.subtitleBlocks(); // todo: use the bulk api
            var doc = new VideoDocument(videoId, variety.name(), sentence.text(), rangesToMap(ranges));
            elasticService.indexDocument(VIDEOS_INDEX, docId, doc);
        });
    }

    public Map<String, Integer> rangesToMap(RangeMap<Integer, Integer> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
