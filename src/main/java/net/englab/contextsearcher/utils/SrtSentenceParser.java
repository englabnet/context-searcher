package net.englab.contextsearcher.utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.models.SrtBlock;
import net.englab.contextsearcher.models.SrtSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SrtSentenceParser {
    private final static Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]+([.!?]|$)");

    public static List<SrtSentence> parse(String srt) {
        return parse(new SrtSubtitles(srt));
    }

    public static List<SrtSentence> parse(SrtSubtitles subtitles) {
        List<SrtSentence> sentences = new ArrayList<>();

        RangeMap<Integer, Integer> ranges = TreeRangeMap.create();
        StringBuilder sentenceBuilder = new StringBuilder();

        for (SrtBlock block : subtitles) {
            String blockText = String.join(" ", block.text());
            int blockIndex = block.id() - 1;
            List<String> parts = splitIntoSentences(blockText);

            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (part.isBlank()) break;

                int startPoint = sentenceBuilder.length();
                concat(sentenceBuilder, part);
                ranges.put(Range.closed(startPoint, sentenceBuilder.length() - 1), blockIndex);

                if (i < parts.size() - 1) {
                    sentences.add(new SrtSentence(ranges, sentenceBuilder.toString()));

                    sentenceBuilder = new StringBuilder();
                    ranges = TreeRangeMap.create();
                }
            }
        }

        return sentences;
    }

    private static List<String> splitIntoSentences(String text) {
        if (text.isBlank()) return List.of();

        List<String> sentences = new ArrayList<>();

        // The regular expression below will split sentences while keeping their ending punctuation.
        Matcher matcher = SENTENCE_PATTERN.matcher(text);

        while (matcher.find()) {
            sentences.add(matcher.group().trim());
        }

        // Check if the last character of the text is one of the punctuation marks
        char lastChar = text.charAt(text.length() - 1);
        if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
            sentences.add("");
        }

        return sentences;
    }

    private static void concat(StringBuilder builder, String str) {
        if (builder.isEmpty()) {
            builder.append(str);
        } else {
            builder.append(" ").append(str);
        }
    }
}
