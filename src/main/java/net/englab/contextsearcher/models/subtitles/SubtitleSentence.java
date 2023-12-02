package net.englab.contextsearcher.models.subtitles;

import com.google.common.collect.RangeMap;

/**
 * Represents a subtitle sentence that holds the text of the sentence
 * and a range map that associates character indices with subtitle entries.
 *
 * @param text      the text content of the subtitle sentence
 * @param position  the position where the sentence starts in the original subtitle entry
 * @param rangeMap  a range map associating character indices within the sentence to corresponding
 *                  subtitle entry indices. This facilitates identifying which part of the sentence
 *                  aligns with specific subtitle entries. For instance, if we have a sentence that
 *                  starts in the first subtitle entry and ends in the second one, the map might look
 *                  like this: [[0..58]=0, [59..130]=1]
 */
public record SubtitleSentence(String text, int position, RangeMap<Integer, Integer> rangeMap) {
}
