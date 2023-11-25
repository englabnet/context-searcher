package net.englab.contextsearcher.models.subtitles;

import net.englab.contextsearcher.models.common.TimeFrame;

import java.util.List;

/**
 * An SRT subtitle entry that represents a block of subtitles in the SRT format.
 *
 * @param id        the identifier of the entry
 * @param timeFrame the time frame of the entry
 * @param text      the text of the entry. This list may contain several lines of text.
 */
public record SrtEntry(int id, TimeFrame timeFrame, List<String> text) {
}
