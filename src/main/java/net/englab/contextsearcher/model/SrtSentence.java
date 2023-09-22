package net.englab.contextsearcher.model;

import com.google.common.collect.RangeMap;

public record SrtSentence(RangeMap<Integer, TimeFrame> timeRanges, String text) {
}
