package net.englab.contextsearcher.models.subtitles;

import com.google.common.collect.RangeMap;

public record SubtitleSentence(RangeMap<Integer, Integer> subtitleBlocks, String text) {
}
