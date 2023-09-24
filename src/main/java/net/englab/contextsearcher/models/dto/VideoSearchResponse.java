package net.englab.contextsearcher.models.dto;

import java.util.List;

public record VideoSearchResponse(long count, List<VideoSearchResult> videos) {
}
