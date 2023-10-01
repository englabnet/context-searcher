package net.englab.contextsearcher.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleBlock {
    private double startTime;
    private double endTime;
    private List<String> text;
}
