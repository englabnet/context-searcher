package net.englab.contextsearcher.models;

import lombok.Data;

import java.util.List;

@Data
public class SubtitleBlock {
    private double startTime;
    private double endTime;
    private List<String> text;
}
