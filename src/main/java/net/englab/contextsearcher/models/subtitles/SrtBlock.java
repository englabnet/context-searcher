package net.englab.contextsearcher.models.subtitles;

import java.util.List;

public record SrtBlock(int id, TimeFrame timeFrame, List<String> text) { }
