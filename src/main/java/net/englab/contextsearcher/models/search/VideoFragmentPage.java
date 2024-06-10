package net.englab.contextsearcher.models.search;

import net.englab.common.search.models.search.VideoFragment;

import java.util.List;

/**
 * Represents a page of video fragments.
 *
 * @param count     the total number of video fragments found across all pages
 * @param videos    a list of video fragments that are present on the current page
 */
public record VideoFragmentPage(long count, List<VideoFragment> videos) {
}
