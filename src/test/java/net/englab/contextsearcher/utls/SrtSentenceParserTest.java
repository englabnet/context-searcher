package net.englab.contextsearcher.utls;

import net.englab.contextsearcher.models.SrtSentence;
import net.englab.contextsearcher.utils.SrtSentenceParser;
import net.englab.contextsearcher.utils.SrtSubtitles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SrtSentenceParserTest {

    @Test
    void testLongSentence() {
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

        SrtSubtitles subtitles = new SrtSubtitles(text);

        List<SrtSentence> sentences = SrtSentenceParser.parse(subtitles);

        assertEquals(1, sentences.size());
        SrtSentence sentence = sentences.get(0);
        String expectedText = "The most common words that languages borrow from each other are nouns, words for things, " +
                "probably since it's fairly straightforward to point at an object, say the word for it, and have the other " +
                "person understand that’s what you’re talking about.";
        assertEquals(expectedText, sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(58));
        assertEquals(1, sentence.subtitleBlocks().get(59));
        assertEquals(1, sentence.subtitleBlocks().get(130));
        assertEquals(2, sentence.subtitleBlocks().get(131));
        assertEquals(2, sentence.subtitleBlocks().get(174));
        assertEquals(3, sentence.subtitleBlocks().get(175));
        assertEquals(3, sentence.subtitleBlocks().get(245));
    }

    @Test
    void testShortSentences() {
        String text = """
                1
                00:00:00,000 --> 00:00:03,000
                Hello world! How are you? I'm fine.
                This is a test
                                
                2
                00:00:03,000 --> 00:00:06,600
                that will catch bugs!
                """;

        SrtSubtitles subtitles = new SrtSubtitles(text);

        List<SrtSentence> sentences = SrtSentenceParser.parse(subtitles);

        assertEquals(4, sentences.size());
        SrtSentence sentence = sentences.get(0);
        assertEquals("Hello world!", sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(11));
        sentence = sentences.get(1);
        assertEquals("How are you?", sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(11));
        sentence = sentences.get(2);
        assertEquals("I'm fine.", sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(8));
        sentence = sentences.get(3);
        assertEquals("This is a test that will catch bugs!", sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(13));
        assertEquals(1, sentence.subtitleBlocks().get(14));
        assertEquals(1, sentence.subtitleBlocks().get(35));
    }

    @Test
    void testShortSentences2() {
        String text = """
                1
                00:01:06,960 --> 00:01:10,860
                The Arabic word /sˤaħ raːʔ/ for "deserts"
                became the English word
                                
                2
                00:01:10,860 --> 00:01:14,460
                specifically for the Sahara Desert.
                So the Desert Desert.
                                
                3
                00:01:14,460 --> 00:01:17,520
                Which keeps happening!
                Matcha tea is "powdered tea tea".
                """;

        SrtSubtitles subtitles = new SrtSubtitles(text);

        List<SrtSentence> sentences = SrtSentenceParser.parse(subtitles);

        assertEquals(4, sentences.size());
        SrtSentence sentence = sentences.get(0);
        String expectedText = "The Arabic word /sˤaħ raːʔ/ for \"deserts\" became " +
                "the English word specifically for the Sahara Desert.";
        assertEquals(expectedText, sentence.text());
        assertEquals(0, sentence.subtitleBlocks().get(0));
        assertEquals(0, sentence.subtitleBlocks().get(64));
        assertEquals(1, sentence.subtitleBlocks().get(65));
        assertEquals(1, sentence.subtitleBlocks().get(100));
        sentence = sentences.get(1);
        assertEquals("So the Desert Desert.", sentence.text());
        assertEquals(1, sentence.subtitleBlocks().get(0));
        assertEquals(1, sentence.subtitleBlocks().get(20));
        sentence = sentences.get(2);
        assertEquals("Which keeps happening!", sentence.text());
        assertEquals(2, sentence.subtitleBlocks().get(0));
        assertEquals(2, sentence.subtitleBlocks().get(21));
        sentence = sentences.get(3);
        assertEquals("Matcha tea is \"powdered tea tea\".", sentence.text());
        assertEquals(2, sentence.subtitleBlocks().get(0));
        assertEquals(2, sentence.subtitleBlocks().get(32));
    }
}
