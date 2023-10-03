package net.englab.contextsearcher.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.models.SubtitleBlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Deprecated
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SrtParser {

    public static List<SubtitleBlock> parseSubtitles(String srt) {
        try (BufferedReader srtReader = new BufferedReader(new StringReader(srt))) {
            List<SubtitleBlock> subtitles = new ArrayList<>();

            String line = srtReader.readLine();
            while (line != null) {
                subtitles.add(parseSubtitleBlock(srtReader));
                line = srtReader.readLine();
            }

            return subtitles;
        } catch (IOException e) {
            log.error("Exception occurred during video indexing", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static SubtitleBlock parseSubtitleBlock(BufferedReader srtReader) {
        SubtitleBlock subtitleBlock = new SubtitleBlock();

        String[] timeInfo = srtReader.readLine().split("-->");
        subtitleBlock.setStartTime(convertToSeconds(timeInfo[0].strip()));
        subtitleBlock.setEndTime(convertToSeconds(timeInfo[1].strip()));

        String line = srtReader.readLine();

        String text = "";
        while (line != null && !line.equals("")) {
            if (text.isBlank()) {
                text = line;
            } else {
                text += " " + line; // todo: fix
            }
            line = srtReader.readLine();
        }
        subtitleBlock.setText(List.of(text));
        return subtitleBlock;
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
}
