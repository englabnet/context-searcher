package net.englab.contextsearcher.models.dto;

import net.englab.contextsearcher.models.EnglishVariety;

public record VideoDto(String videoId, EnglishVariety variety, String srt) {
}
