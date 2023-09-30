package net.englab.contextsearcher.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class SrtSubtitles implements Iterable<SrtSubtitles.SrtBlock> {
    private final List<SrtBlock> srtBlocks;

    public SrtSubtitles(String srt) {
        srtBlocks = parseSrtBlocks(srt);
    }

    private static List<SrtBlock> parseSrtBlocks(String srt) {
        try (BufferedReader srtReader = new BufferedReader(new StringReader(srt))) {
            List<SrtBlock> result = new ArrayList<>();

            String line = srtReader.readLine();
            while (line != null) {
                int id = Integer.parseInt(line);
                String timeFrame = srtReader.readLine();

                List<String> text = new ArrayList<>();
                line = srtReader.readLine();
                while (line != null && !line.equals("")) {
                    text.add(line);
                    line = srtReader.readLine();
                }

                result.add(new SrtBlock(id, timeFrame, text));

                line = srtReader.readLine();
            }

            return result;
        } catch (IOException e) {
            log.error("Exception occurred while parsing subtitles", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<SrtBlock> iterator() {
        return srtBlocks.iterator();
    }

    public record SrtBlock(int id, String timeFrame, List<String> text) { }
}
