package net.englab.contextsearcher.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
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
                TimeFrame timeFrame = parseTimeFrame(srtReader.readLine());

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

    private static TimeFrame parseTimeFrame(String line) {
        String[] timeInfo = line.split("-->");
        double startTime = convertToSeconds(timeInfo[0].strip());
        double endTime = convertToSeconds(timeInfo[1].strip());
        return new TimeFrame(startTime, endTime);
    }

    private static double convertToSeconds(String timeString) {
        String formattedTimeString = "PT"
                + timeString.replaceFirst(":", "H")
                .replaceFirst(":", "M")
                .replace(",", ".")
                + "S";

        Duration duration = Duration.parse(formattedTimeString);
        return duration.getSeconds() + duration.getNano() / 1_000_000_000.0;
    }

    @Override
    public Iterator<SrtBlock> iterator() {
        return srtBlocks.iterator();
    }

    /**
     * Returns an immutable copy of srt blocks.
     */
    public List<SrtBlock> getSrtBlocks() {
        return List.copyOf(srtBlocks);
    }

    public record SrtBlock(int id, TimeFrame timeFrame, List<String> text) { }

    public record TimeFrame(double startTime, double endTime) {}
}
