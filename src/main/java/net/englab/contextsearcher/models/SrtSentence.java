package net.englab.contextsearcher.models;

import com.google.common.collect.RangeMap;

public record SrtSentence(RangeMap<Integer, Integer> subtitleBlocks, String text) {
}
