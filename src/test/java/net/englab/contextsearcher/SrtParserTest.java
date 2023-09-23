package net.englab.contextsearcher;

import net.englab.contextsearcher.model.SrtSentence;
import net.englab.contextsearcher.model.TimeFrame;
import net.englab.contextsearcher.utils.SrtParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SrtParserTest {

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

        List<SrtSentence> sentences = SrtParser.parseSentences(text);

        assertEquals(1, sentences.size());
        SrtSentence sentence = sentences.get(0);
        String expectedText = "The most common words that languages borrow from each other are nouns, words for things, " +
                "probably since it's fairly straightforward to point at an object, say the word for it, and have the other " +
                "person understand that’s what you’re talking about.";
        assertEquals(expectedText, sentence.text());
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(58));
        assertEquals(new TimeFrame(3, 7), sentence.timeRanges().get(59));
        assertEquals(new TimeFrame(3, 7), sentence.timeRanges().get(130));
        assertEquals(new TimeFrame(6, 9), sentence.timeRanges().get(131));
        assertEquals(new TimeFrame(6, 9), sentence.timeRanges().get(174));
        assertEquals(new TimeFrame(8, 12), sentence.timeRanges().get(175));
        assertEquals(new TimeFrame(8, 12), sentence.timeRanges().get(246));
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

        List<SrtSentence> sentences = SrtParser.parseSentences(text);

        assertEquals(4, sentences.size());
        SrtSentence sentence = sentences.get(0);
        assertEquals("Hello world!", sentence.text());
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(12));
        sentence = sentences.get(1);
        assertEquals("How are you?", sentence.text());
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(11));
        sentence = sentences.get(2);
        assertEquals("I'm fine.", sentence.text());
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(8));
        sentence = sentences.get(3);
        assertEquals("This is a test that will catch bugs!", sentence.text());
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(0, 3), sentence.timeRanges().get(13));
        assertEquals(new TimeFrame(3, 7), sentence.timeRanges().get(14));
        assertEquals(new TimeFrame(3, 7), sentence.timeRanges().get(36));
    }

    @Test
    void testShortSentences2() {
        String text = """
                22
                00:01:06,960 --> 00:01:10,860
                The Arabic word /sˤaħ raːʔ/ for "deserts"
                became the English word
                                
                23
                00:01:10,860 --> 00:01:14,460
                specifically for the Sahara Desert.
                So the Desert Desert.
                                
                24
                00:01:14,460 --> 00:01:17,520
                Which keeps happening!
                Matcha tea is "powdered tea tea".
                """;

        List<SrtSentence> sentences = SrtParser.parseSentences(text);

        assertEquals(4, sentences.size());
        SrtSentence sentence = sentences.get(0);
        String expectedText = "The Arabic word /sˤaħ raːʔ/ for \"deserts\" became " +
                "the English word specifically for the Sahara Desert.";
        assertEquals(expectedText, sentence.text());
        assertEquals(new TimeFrame(66, 71), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(66, 71), sentence.timeRanges().get(64));
        assertEquals(new TimeFrame(70, 75), sentence.timeRanges().get(65));
        assertEquals(new TimeFrame(70, 75), sentence.timeRanges().get(101));
        sentence = sentences.get(1);
        assertEquals("So the Desert Desert.", sentence.text());
        assertEquals(new TimeFrame(70, 75), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(70, 75), sentence.timeRanges().get(21));
        sentence = sentences.get(2);
        assertEquals("Which keeps happening!", sentence.text());
        assertEquals(new TimeFrame(74, 78), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(74, 78), sentence.timeRanges().get(22));
        sentence = sentences.get(3);
        assertEquals("Matcha tea is \"powdered tea tea\".", sentence.text());
        assertEquals(new TimeFrame(74, 78), sentence.timeRanges().get(0));
        assertEquals(new TimeFrame(74, 78), sentence.timeRanges().get(33));
    }
}
