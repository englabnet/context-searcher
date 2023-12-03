package net.englab.contextsearcher.subtitles;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.models.subtitles.SrtEntry;
import net.englab.contextsearcher.models.common.TimeFrame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A class that parses SRT subtitles, providing various methods for the further processing of SRT entries.
 */
@Slf4j
public class SrtSubtitles implements Iterable<SrtEntry> {
    // This pattern is used to replace any unusual separator characters with spaces
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[\\p{Z}\\s]");

    private final List<SrtEntry> srtEntries;

    /**
     * Create new SRT subtitles.
     *
     * @param srt text that represents subtitles in the SRT format
     */
    public SrtSubtitles(String srt) {
        srtEntries = parseSrtEntries(srt);
    }

    private static List<SrtEntry> parseSrtEntries(String srt) {
        try (BufferedReader srtReader = new BufferedReader(new StringReader(srt))) {
            List<SrtEntry> result = new ArrayList<>();

            String line = srtReader.readLine();
            while (line != null && !line.isBlank()) {
                int id = Integer.parseInt(line);
                TimeFrame timeFrame = parseTimeFrame(srtReader.readLine());

                List<String> text = new ArrayList<>();
                line = srtReader.readLine();
                do {
                    parseTextLine(line).ifPresent(text::add);
                    line = srtReader.readLine();
                } while (line != null && !line.isEmpty());

                result.add(new SrtEntry(id, timeFrame, text));

                line = srtReader.readLine();
            }

            return result;
        } catch (IOException e) {
            log.error("Exception occurred while parsing subtitles", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static Optional<String> parseTextLine(String line) {
        if (line != null) {
            String text = SEPARATOR_PATTERN.matcher(line).replaceAll(" ");
            return Optional.of(text);
        }
        return Optional.empty();
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

    /**
     * Returns a read-only iterator over the SRT entries.
     */
    @Override
    public Iterator<SrtEntry> iterator() {
        return Collections.unmodifiableList(srtEntries).iterator();
    }

    /**
     * Returns a stream with the SRT entries as its source.
     */
    public Stream<SrtEntry> stream() {
        return srtEntries.stream();
    }

    /**
     * Returns the number of SRT entries.
     */
    public int size() {
        return srtEntries.size();
    }

    /**
     * Returns the SRT entry at the specified position.
     */
    public SrtEntry get(int index) {
        return srtEntries.get(index);
    }
}
