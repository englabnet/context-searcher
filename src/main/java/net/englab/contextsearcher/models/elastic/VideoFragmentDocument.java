package net.englab.contextsearcher.models.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.RangeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.models.common.EnglishVariety;

import static net.englab.contextsearcher.elastic.VideoIndexProperties.*;

/**
 * Represents a video fragment stored in Elasticsearch.
 * A video fragment holds only one sentence of text.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoFragmentDocument {

    /**
     * The YouTube video ID.
     */
    @JsonProperty(YOUTUBE_VIDEO_ID)
    private String youtubeVideoId;

    /**
     * The variety of English that is used in the video fragment.
     */
    @JsonProperty(ENGLISH_VARIETY)
    private EnglishVariety variety;

    /**
     * The text of the sentence.
     */
    @JsonProperty(SENTENCE)
    private String sentence;

    /**
     * The position where the sentence starts in the original subtitle entry.
     * (It is used for highlighting)
     */
    @JsonProperty(SENTENCE_POSITION)
    private int sentencePosition;

    /**
     * A range map associating character indices within the sentence to corresponding
     * subtitle entry indices. For example, it might look like this:
     * { "[0..49)": 295, "[49..83)": 296 "[83..121)": 297, "[121..153)": 298 }
     * For simplicity, it stores ranges as strings.
     * @see net.englab.contextsearcher.models.subtitles.SubtitleSentence
     */
    @JsonProperty(SENTENCE_RANGE_MAP)
    private RangeMap<Integer, Integer> sentenceRangeMap;
}
