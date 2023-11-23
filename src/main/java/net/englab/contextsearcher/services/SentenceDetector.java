package net.englab.contextsearcher.services;

import lombok.SneakyThrows;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * A service that analyses the given text and split it into sentences.
 * This implementation is using Machine Learning to detect sentences correctly.
 */
@Service
public class SentenceDetector {
    private static final String MODEL_FILENAME = "models/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin";
    private final SentenceDetectorME sentenceDetector;

    @SneakyThrows
    public SentenceDetector(ResourceLoader resourceLoader) {
        Resource modelResource = resourceLoader.getResource("classpath:" + MODEL_FILENAME);
        SentenceModel model = new SentenceModel(modelResource.getInputStream());
        sentenceDetector = new SentenceDetectorME(model);
    }

    /**
     * Analyses the given text and detects sentences in it.
     *
     * @param text the text that we want to analyse
     * @return an array of spans
     */
    public Span[] detect(String text) {
        return sentenceDetector.sentPosDetect(text);
    }
}
