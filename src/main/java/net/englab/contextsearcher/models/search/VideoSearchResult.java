package net.englab.contextsearcher.models.search;

import java.util.List;

/**
 * A video search result.
 *
 * @param count     the total number of video fragments
 * @param videos    the list of video fragments on the current page
 */
public record VideoSearchResult(long count, List<VideoFragment> videos) {
}
