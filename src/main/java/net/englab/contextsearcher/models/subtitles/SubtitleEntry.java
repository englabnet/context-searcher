package net.englab.contextsearcher.models.subtitles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// TODO: turn it into a record
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleEntry {
    private double startTime;
    private double endTime;
    private List<String> text;
}
