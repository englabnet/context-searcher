package net.englab.contextsearcher.models;

import com.google.common.collect.RangeMap;

public record SrtSentence(RangeMap<Integer, TimeFrame> timeRanges, String text) {
}
