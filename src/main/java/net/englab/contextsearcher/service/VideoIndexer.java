package net.englab.contextsearcher.service;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import com.google.common.collect.RangeMap;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.elastic.VideoDocument;
import net.englab.contextsearcher.model.SrtSentence;
import net.englab.contextsearcher.model.TimeFrame;
import net.englab.contextsearcher.utils.SrtParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.englab.contextsearcher.elastic.ElasticProperties.*;

@Service
@RequiredArgsConstructor
public class VideoIndexer {

    private final ElasticService elasticService;

    public void indexVideo(String videoId, String srt) {
        List<SrtSentence> sentences = SrtParser.parseSentences(srt);
        elasticService.createIndexIfAbsent("videos", Map.of(
                "video_id", NON_SEARCHABLE_TEXT_PROPERTY,
                "sentence", TEXT_PROPERTY,
                "time_ranges", ObjectProperty.of(b -> b.enabled(false))._toProperty()
        ));

        sentences.forEach(sentence -> {
            String id = UUID.randomUUID().toString();
            RangeMap<Integer, TimeFrame> ranges = sentence.timeRanges(); // todo: use the bulk api
            elasticService.indexDocument("videos", id, new VideoDocument(videoId, sentence.text(), rangesToMap(ranges)));
        });
    }

    public Map<String, TimeFrame> rangesToMap(RangeMap<Integer, TimeFrame> ranges) {
        return ranges.asMapOfRanges().entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey().toString(), Map.Entry::getValue));
    }
}
