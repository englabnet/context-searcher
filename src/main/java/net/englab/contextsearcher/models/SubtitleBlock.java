package net.englab.contextsearcher.models;

import lombok.Data;

@Data
public class SubtitleBlock {
    private double startTime;
    private double endTime;
    private String text = "";
}
