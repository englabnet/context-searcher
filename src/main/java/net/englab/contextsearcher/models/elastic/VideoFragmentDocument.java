package net.englab.contextsearcher.models.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.RangeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.models.common.EnglishVariety;

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
    @JsonProperty("video_id")
    private String videoId;

    /**
     * The variety of English that is used in the video fragment.
     */
    private EnglishVariety variety;

    /**
     * The text of the sentence.
     */
    private String sentence;

    /**
     * A range map associating character indices within the sentence to corresponding
     * subtitle entry indices. For example, it might look like this:
     * { "[0..49)": 295, "[49..83)": 296 "[83..121)": 297, "[121..153)": 298 }
     * For simplicity, it stores ranges as strings.
     * @see net.englab.contextsearcher.models.subtitles.SubtitleSentence
     */
    @JsonProperty("subtitle_blocks")  // TODO: rename it to subtitle_range_map
    private RangeMap<Integer, Integer> sentenceRangeMap;
}
