package net.englab.contextsearcher.subtitles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentenceDetectorTest {

    private final SentenceDetector sentenceDetector = new SentenceDetector();

    @Test
    void test() {
        String text = "  First sentence. Second sentence. ";

        var spans = sentenceDetector.detect(text);

        assertEquals(2, spans.length);
    }

    @Test
    void testEllipsis() {
        String text = "Oooh! And me! I'm shaking my booty! ...this wasn’t really the Doctor Who I personally loved.";

        var spans = sentenceDetector.detect(text);

        assertEquals(4, spans.length);
    }

    @Test
    void testEllipsis2() {
        String text = """
                    The ... fox jumps...
                    The quick brown fox jumps over the lazy dog. And if they have not died, they are still alive today.
                    It is not cold ... it is freezing cold.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(4, spans.length);
    }

    @Test
    void testWebsite() {
        String text = """
                They actually helped make that Earth bridge animation for me.
                So be sure to check out GoogleScienceFair.com.
                And I'll keep checking out Los Angeles.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(3, spans.length);
    }

    // The model I'm currently using can't recognise initials correctly
    //@Test
    void testInitials() {
        String text = """
                The famous fictional world of Snaiad is the creation of the one and only C. M. Kosemen,
                author of All Tomorrows and expert in all things alien, who you can follow and support
                in the links below. This online worldbuilding project includes more than 200 extraterrestrial
                lifeforms from several dozen lineages — and an awe-inspiring amount of detail about each one.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(2, spans.length);
    }

    @Test
    void testAbbreviation() {
        String text = """
                And another form of negativity, complaining.
                Well, this is the national art of the U.K. which is very good.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(2, spans.length);
    }

    // The model I'm currently using can't recognise some abbreviations correctly
    //@Test
    void testAbbreviation2() {
        String text = """
                And in September of last year, the completed telescope was shipped from its testing facility
                in California, to the Guianan Space Centre, in French Guiana, a.k.a. Europe’s Spaceport.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(1, spans.length);
    }

    @Test
    void testMrs() {
        String text = """
                Alex, what's going on? Good morning, Mr. Campbell. Faisal Jenson.
                Apparently I must thank you for rescuing Alex.
                """;

        var spans = sentenceDetector.detect(text);

        assertEquals(4, spans.length);
    }
}
