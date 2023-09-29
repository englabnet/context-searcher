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

            RangeMap<Integer, Integer> currentBlocks = TreeRangeMap.create();
            String currentSentence = "";

            String line = srtReader.readLine();
            while (line != null) {
                Integer currentIndex = Integer.parseInt(line) - 1;
                srtReader.readLine(); // Time frame
                line = srtReader.readLine();
                int startPoint = currentSentence.length();
                while (line != null && !line.equals("")) {
                    List<String> str = splitIntoSentences(line);
                    if (str.size() > 1) {
                        var ending = str.get(0);
                        var text = concatStrings(currentSentence, ending);

                        currentBlocks.put(Range.closed(startPoint, text.length()), currentIndex);

                        sentences.add(new SrtSentence(currentBlocks, text));
                        currentSentence = "";
                        currentBlocks = TreeRangeMap.create();
                        startPoint = 0;
                    }
                    for (int i = 1; i < str.size() - 1; i++) {
                        var sentence = str.get(i);
                        currentBlocks.put(Range.closed(0, sentence.length() - 1), currentIndex);
                        sentences.add(new SrtSentence(currentBlocks, str.get(i)));
                        currentBlocks = TreeRangeMap.create();
                    }
                    String rest = str.get(str.size() - 1);
                    currentSentence = concatStrings(currentSentence, rest);
                    line = srtReader.readLine();
                }
                if (!currentSentence.isEmpty()) {
                    currentBlocks.put(Range.closed(startPoint, currentSentence.length()), currentIndex);
                }
                line = srtReader.readLine();
            }

            if (!currentSentence.isEmpty()) {
                sentences.add(new SrtSentence(currentBlocks, currentSentence));
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
