package net.englab.contextsearcher.subtitles;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import net.englab.contextsearcher.models.subtitles.SrtEntry;
import net.englab.contextsearcher.models.subtitles.SubtitleSentence;
import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * A class that is used to extract sentences from subtitles in the SRT format.
 */
public class SubtitleSentenceExtractor {

    private final SentenceDetector sentenceDetector = new SentenceDetector();

    /**
     * Extracts sentences from the given SRT subtitles.
     *
     * @param srt the subtitles in the SRT format
     * @return a collection of subtitle sentences
     */
    public List<SubtitleSentence> extract(String srt) {
        SrtSubtitles srtSubtitles = new SrtSubtitles(srt);

        StringJoiner stringJoiner = new StringJoiner(" ");

        // character position -> SRT entry index
        // the range map shows in which SRT entry a specific piece of text appears
        RangeMap<Integer, Integer> textRangeMap = TreeRangeMap.create();

        // concatenate all the text into one string and build a range map for it
        int srtEntryIndex = 0;
        for (SrtEntry srtEntry : srtSubtitles) {
            String entryText = String.join(" ", srtEntry.text());

            int textLength = stringJoiner.length();
            textRangeMap.put(Range.closedOpen(textLength, textLength + entryText.length() + 1), srtEntryIndex);

            if (!entryText.isBlank()) {
                stringJoiner.add(entryText);
            }

            srtEntryIndex++;
        }

        String text = stringJoiner.toString();

        Span[] spans = sentenceDetector.detect(text);

        // go through the detected sentences and build the result collection
        List<SubtitleSentence> sentences = new ArrayList<>();
        for (Span span : spans) {
            String sentenceText = text.substring(span.getStart(), span.getEnd());

            Range<Integer> sentenceRange = Range.closedOpen(span.getStart(), span.getEnd());
            RangeMap<Integer, Integer> sentenceRangeMap = normalizeRangeMap(textRangeMap.subRangeMap(sentenceRange));

            SubtitleSentence sentence = new SubtitleSentence(sentenceText, sentenceRangeMap);
            sentences.add(sentence);
        }

        return sentences;
    }

    /**
     * Normalises the given range map by shifting all the ranges to zero.
     * For example, if we have the following range map: [36..50) -> 0, [50..72) -> 1
     * The normalised range map will be: [0..14) -> 0, [14..36) -> 1
     */
    private RangeMap<Integer, Integer> normalizeRangeMap(RangeMap<Integer, Integer> rangeMap) {
        RangeMap<Integer, Integer> normalizedRangeMap = TreeRangeMap.create();

        int offset = -1;
        var mapOfRanges = rangeMap.asMapOfRanges();

        for (var entry : mapOfRanges.entrySet()) {
            Range<Integer> range = entry.getKey();

            if (offset == -1) {
                offset = entry.getKey().lowerEndpoint();
            }

            int recalculatedLowerEndpoint = range.lowerEndpoint() - offset;
            int recalculatedUpperEndpoint = range.upperEndpoint() - offset;

            Range<Integer> recalculatedRange = Range.range(
                    recalculatedLowerEndpoint, range.lowerBoundType(),
                    recalculatedUpperEndpoint, range.upperBoundType()
            );

            normalizedRangeMap.put(recalculatedRange, entry.getValue());
        }

        return normalizedRangeMap;
    }
}
