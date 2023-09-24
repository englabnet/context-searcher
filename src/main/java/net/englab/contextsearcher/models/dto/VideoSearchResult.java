package net.englab.contextsearcher.models.dto;

import net.englab.contextsearcher.models.SubtitleBlock;
import net.englab.contextsearcher.models.TimeFrame;

import java.util.List;

public record VideoSearchResult(String videoId, TimeFrame timeFrame, List<SubtitleBlock> subtitles) {
}
