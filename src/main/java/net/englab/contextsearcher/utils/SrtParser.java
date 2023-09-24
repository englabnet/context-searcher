package net.englab.contextsearcher.utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.models.SubtitleBlock;
import net.englab.contextsearcher.models.TimeFrame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SrtParser {

    private final static Pattern sentencePattern = Pattern.compile("[^.!?]+([.!?]|$)");

    public static List<SrtSentence> parseSentences(String srt) {
        try (BufferedReader srtReader = new BufferedReader(new StringReader(srt))) {
            List<SrtSentence> sentences = new ArrayList<>();

            RangeMap<Integer, TimeFrame> currentTimeFrames = TreeRangeMap.create();
            String currentSentence = "";

            String line = srtReader.readLine();
            while (line != null) {
                TimeFrame timeFrame = parseTimeFrame(srtReader.readLine());
                line = srtReader.readLine();
                int startPoint = currentSentence.length();
                while (line != null && !line.equals("")) {
                    List<String> str = splitIntoSentences(line);
                    if (str.size() > 1) {
                        var ending = str.get(0);
                        var text = concatStrings(currentSentence, ending);

                        currentTimeFrames.put(Range.closed(startPoint, text.length()), timeFrame);

                        sentences.add(new SrtSentence(currentTimeFrames, text));
                        currentSentence = "";
                        currentTimeFrames = TreeRangeMap.create();
                        startPoint = 0;
                    }
                    for (int i = 1; i < str.size() - 1; i++) {
                        var sentence = str.get(i);
                        currentTimeFrames.put(Range.closed(0, sentence.length() - 1), timeFrame);
                        sentences.add(new SrtSentence(currentTimeFrames, str.get(i)));
                        currentTimeFrames = TreeRangeMap.create();
                    }
                    String rest = str.get(str.size() - 1);
                    currentSentence = concatStrings(currentSentence, rest);
                    line = srtReader.readLine();
                }
                line = srtReader.readLine();
                if (!currentSentence.isEmpty()) {
                    currentTimeFrames.put(Range.closed(startPoint, currentSentence.length()), timeFrame);
                }
            }

            if (!currentSentence.isEmpty()) {
                sentences.add(new SrtSentence(currentTimeFrames, currentSentence));
            }

            return sentences;
        } catch (IOException e) {
            log.error("Exception occurred during video indexing", e);
            throw new RuntimeException(e);
        }
    }

    private static String concatStrings(String a, String b) {
        if (a.isEmpty()) {
            return b;
        } else {
            return a + " " + b; // todo: fix
        }
    }

    private static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();

        // The regular expression below will split sentences while keeping their ending punctuation.
        Matcher matcher = sentencePattern.matcher(text);

        while (matcher.find()) {
            sentences.add(matcher.group().trim());
        }

        // Check if the last character of the text is one of the punctuation marks
        char lastChar = text.charAt(text.length() - 1);
        if ((lastChar == '.' || lastChar == '!' || lastChar == '?')) {
            sentences.add("");
        }

        return sentences;
    }

    private static TimeFrame parseTimeFrame(String line) {
        String[] timeInfo = line.split("-->");
        double startTime = convertToSeconds(timeInfo[0].strip());
        double endTime = convertToSeconds(timeInfo[1].strip());
        return new TimeFrame(startTime, endTime);
    }

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
        while (line != null && !line.equals("")) {
            String previousText = subtitleBlock.getText();
            if (previousText.isEmpty()) {
                subtitleBlock.setText(line);
            } else {
                subtitleBlock.setText(previousText + " " + line);
            }
            line = srtReader.readLine();
        }
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
