package net.englab.contextsearcher.models.subtitles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a subtitle entry of a video.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleEntry {
    // TODO: use TimeFrame here

    /**
     * The start time of the subtitle entry in seconds.
     */
    private double startTime;

    /**
     * The end time of the subtitle entry in seconds.
     */
    private double endTime;

    /**
     * The text of the subtitles. This list may contain multiple elements
     * if there are highlighted parts in the text. In such cases, each even-indexed
     * element represents a highlighted part of the text.
     */
    private List<String> text;
}
