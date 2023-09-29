package net.englab.contextsearcher.models.dto;

import net.englab.contextsearcher.models.SubtitleBlock;

import java.util.List;

public record VideoSearchResult(
        String videoId,
        Integer index,
        List<SubtitleBlock> subtitles
) { }
