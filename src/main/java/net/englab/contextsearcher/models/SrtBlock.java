package net.englab.contextsearcher.models;

import java.util.List;

public record SrtBlock(int id, TimeFrame timeFrame, List<String> text) { }
