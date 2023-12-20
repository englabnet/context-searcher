package net.englab.contextsearcher.models.subtitles;

import java.util.List;

/**
 * Represents a subtitle entry of a video.
 *
 * @param startTime The start time of the subtitle entry in seconds.
 * @param endTime   The end time of the subtitle entry in seconds.
 * @param text      The text of the subtitles. This list may contain multiple elements
 *                  if there are highlighted parts in the text. In such cases, each even-indexed
 *                  element represents a highlighted part of the text.
 */
public record SubtitleEntry(double startTime, double endTime, List<String> text) {
}
