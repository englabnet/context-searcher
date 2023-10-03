package net.englab.contextsearcher.utls;

import net.englab.contextsearcher.models.SubtitleBlock;
import net.englab.contextsearcher.utils.SubtitleHighlighter;
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
        List<SubtitleBlock> blocks = List.of(
                new SubtitleBlock(0.0, 0.0, List.of("your Lorentz factor by now is 650. And the")),
                new SubtitleBlock(0.0, 0.0, List.of("left over radiation from the Big Bang, the Cosmic Microwave Background, has now")),
                new SubtitleBlock(0.0, 0.0, List.of("blue shifted all the way from the microwave to visible red light. Over the"))
        );


        SubtitleHighlighter.highlight(sentence, parts, blocks);

        List<SubtitleBlock> expectedBlocks = List.of(
                new SubtitleBlock(0.0, 0.0, List.of("your Lorentz factor by now is 650. And ", "the")),
                new SubtitleBlock(0.0, 0.0, List.of("left over radiation from ", "the", " Big Bang, ", "the", " Cosmic Microwave Background, has now")),
                new SubtitleBlock(0.0, 0.0, List.of("blue shifted all ", "the", " way from ", "the", " microwave to visible red light. Over the"))
        );

        assertEquals(expectedBlocks, blocks);
    }
}
