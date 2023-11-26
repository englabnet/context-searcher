package net.englab.contextsearcher.subtitles;

import lombok.SneakyThrows;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import java.io.InputStream;

/**
 * A class that analyses the given text and splits it into sentences.
 * This implementation is using Machine Learning to detect sentences correctly.
 */
public class SentenceDetector {
    private static final String MODEL_FILENAME = "/models/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin";
    private final SentenceDetectorME sentenceDetectorMe;

    @SneakyThrows
    public SentenceDetector() {
        try (InputStream inputStream = SentenceDetector.class.getResourceAsStream(MODEL_FILENAME)) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot load a model for sentence detection.");
            }
            SentenceModel model = new SentenceModel(inputStream);
            sentenceDetectorMe = new SentenceDetectorME(model);
        }
    }

    /**
     * Analyses the given text and detects sentences in it.
     *
     * @param text the text that we want to analyse
     * @return  an array of spans. Each span contains information
     *          about where the detected sentence begins and ends.
     */
    public Span[] detect(String text) {
        return sentenceDetectorMe.sentPosDetect(text);
    }
}
