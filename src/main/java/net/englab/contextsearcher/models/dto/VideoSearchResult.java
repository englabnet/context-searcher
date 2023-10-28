package net.englab.contextsearcher.models.dto;

import java.util.List;

public record VideoSearchResult(
        String videoId,
        Integer index,
        List<SubtitleBlock> subtitles
) {
}
