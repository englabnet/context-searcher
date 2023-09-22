package net.englab.contextsearcher.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.model.TimeFrame;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDocument {
    @JsonProperty("video_id")
    private String videoId;
    private String sentence;
    @JsonProperty("time_ranges")
    private Map<String, TimeFrame> timeRanges;
}
