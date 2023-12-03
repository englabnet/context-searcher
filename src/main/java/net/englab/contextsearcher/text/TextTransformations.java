package net.englab.contextsearcher.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that contains useful text transformations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextTransformations {

    private static final String SOUND_DESCRIPTION_REGEX = "\\[.*?]|\\(.*?\\)|♫.*?♫|♪.*?♪|\\*.*?\\*";
    private static final Pattern SOUND_DESCRIPTION_PATTERN = Pattern.compile(SOUND_DESCRIPTION_REGEX);

    /**
     * Removes all sound descriptions from the text.
     * For example:
     * <pre>
     * [intense music]
     * (noises)
     * ♫ smooth jazz ♫
     * ♪ lively music ♪
     * *Outro Music*
     * </pre>
     * Note: It is not actually removing parts of the texts,
     * but replacing characters with the non-searchable character '_'.
     * We do that in order to keep the same positions of characters because
     * it helps calculate highlighting correctly.
     *
     * @param text the input text
     * @return the input text without sound descriptions
     */
    public static String removeSoundDescriptions(String text) {
        Matcher matcher = SOUND_DESCRIPTION_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text);
        while (matcher.find()) {
            // Replace each character in the matched region with a space
            for (int i = matcher.start(); i < matcher.end(); i++) {
                result.setCharAt(i, '_');
            }
        }
        return result.toString();
    }
}
