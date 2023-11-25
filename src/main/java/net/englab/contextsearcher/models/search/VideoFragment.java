package net.englab.contextsearcher.models.search;

import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.List;

/**
 * Represents a specific fragment of a video.
 *
 * @param videoId   the YouTube video ID
 * @param index     the index representing the position of
 *                  this fragment within the list of subtitle entries
 * @param subtitles the list of subtitle entries associated with the video
 */
public record VideoFragment(
        String videoId, // TODO: rename it to youtubeVideoId
        Integer index, // TODO: rename it to subtitleEntryIndex
        List<SubtitleEntry> subtitles
) {
}
