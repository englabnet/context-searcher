package net.englab.contextsearcher.subtitles;

import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtitleHighlighterTest {

    @Test
    void test() {
        String sentence = "And the left over radiation from the Big Bang, the Cosmic Microwave Background, " +
                "has now blue shifted all the way from the microwave to visible red light.";
        String[] parts = {
                "And ",
                "the",
                " left over radiation from ",
                "the",
                " Big Bang, ",
                "the",
                " Cosmic Microwave Background, has now blue shifted all ",
                "the",
                " way from ",
                "the",
                " microwave to visible red light."
        };
        List<SubtitleEntry> blocks = List.of(
                new SubtitleEntry(0.0, 0.0, List.of("your Lorentz factor by now is 650. And the")),
                new SubtitleEntry(0.0, 0.0, List.of("left over radiation from the Big Bang, the Cosmic Microwave Background, has now")),
                new SubtitleEntry(0.0, 0.0, List.of("blue shifted all the way from the microwave to visible red light. Over the"))
        );


        SubtitleHighlighter.highlight(sentence, parts, blocks);

        List<SubtitleEntry> expectedBlocks = List.of(
                new SubtitleEntry(0.0, 0.0, List.of("your Lorentz factor by now is 650. And ", "the")),
                new SubtitleEntry(0.0, 0.0, List.of("left over radiation from ", "the", " Big Bang, ", "the", " Cosmic Microwave Background, has now")),
                new SubtitleEntry(0.0, 0.0, List.of("blue shifted all ", "the", " way from ", "the", " microwave to visible red light. Over the"))
        );

        assertEquals(expectedBlocks, blocks);
    }

    @Test
    void emptyBlockTest() {
        String sentence = "'I say chaps last one to arrive in Manchester has to telephone the Queen and a blow raspberry'  " +
                "But little did Claude Grahame White know that his expensive hobby things were soon to become a major military importance.";
        String[] parts = {
                "'I say chaps last one to arrive in Manchester has to telephone the Queen and a blow raspberry' But ",
                "little",
                " ",
                "did",
                " Claude Grahame White know that his expensive hobby things were soon to become a major military importance."
        };
        List<SubtitleEntry> blocks = List.of(
                new SubtitleEntry(0.0, 0.0, List.of("'I say chaps last one to arrive in Manchester has to telephone the Queen and a blow raspberry'")),
                new SubtitleEntry(0.0, 0.0, List.of("")),
                new SubtitleEntry(0.0, 0.0, List.of("But little did Claude Grahame White know that his expensive hobby things")),
                new SubtitleEntry(0.0, 0.0, List.of("were soon to become a major military importance."))
        );


        SubtitleHighlighter.highlight(sentence, parts, blocks);

        List<SubtitleEntry> expectedBlocks = List.of(
                new SubtitleEntry(0.0, 0.0, List.of("'I say chaps last one to arrive in Manchester has to telephone the Queen and a blow raspberry'")),
                new SubtitleEntry(0.0, 0.0, List.of("")),
                new SubtitleEntry(0.0, 0.0, List.of("But ", "little", " ", "did", " Claude Grahame White know that his expensive hobby things")),
                new SubtitleEntry(0.0, 0.0, List.of("were soon to become a major military importance."))
        );

        assertEquals(expectedBlocks, blocks);
    }
}
