package net.englab.contextsearcher.models.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDocument {
    @JsonProperty("video_id")
    private String videoId;
    private String variety;
    private String sentence;
    @JsonProperty("subtitle_blocks")
    private Map<String, Integer> subtitleBlocks;
}
