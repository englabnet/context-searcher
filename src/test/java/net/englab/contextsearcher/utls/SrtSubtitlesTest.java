package net.englab.contextsearcher.utls;

import net.englab.contextsearcher.utils.SrtSubtitles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.englab.contextsearcher.utils.SrtSubtitles.*;
import static org.junit.jupiter.api.Assertions.*;

class SrtSubtitlesTest {

    @Test
    void test() {
        String text = """
                1
                00:00:00,000 --> 00:00:03,000
                The most common words that languages
                borrow from each other
                                
                2
                00:00:03,000 --> 00:00:06,600
                are nouns, words for things, probably
                since it's fairly straightforward
                                
                3
                00:00:06,600 --> 00:00:08,880
                to point at an object,
                say the word for it,
                                
                4
                00:00:08,880 --> 00:00:11,760
                and have the other person understand
                that’s what you’re talking about.
                """;

        SrtSubtitles srtSubtitles = new SrtSubtitles(text);

        List<SrtBlock> expectedResult = List.of(
                new SrtBlock(1, "00:00:00,000 --> 00:00:03,000",
                        List.of("The most common words that languages",
                                "borrow from each other")),
                new SrtBlock(2, "00:00:03,000 --> 00:00:06,600",
                        List.of("are nouns, words for things, probably",
                                "since it's fairly straightforward")),
                new SrtBlock(3, "00:00:06,600 --> 00:00:08,880",
                        List.of("to point at an object,",
                                "say the word for it,")),
                new SrtBlock(4, "00:00:08,880 --> 00:00:11,760",
                        List.of("and have the other person understand",
                                "that’s what you’re talking about."))
        );

        srtSubtitles.forEach(srtBlock ->
                assertEquals(expectedResult.get(srtBlock.id() - 1), srtBlock)
        );
    }

}
