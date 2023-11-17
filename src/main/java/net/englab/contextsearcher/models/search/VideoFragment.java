package net.englab.contextsearcher.models.search;

import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.List;

/**
 * A video fragment record.
 *
 * @param videoId   the YouTube video id
 * @param index     the index of the subtitle block this fragment is referring to
 * @param subtitles the video subtitles
 */
public record VideoFragment(
        String videoId,
        Integer index, // TODO: rename it to subtitleBlockIndex
        List<SubtitleEntry> subtitles
) {
}
