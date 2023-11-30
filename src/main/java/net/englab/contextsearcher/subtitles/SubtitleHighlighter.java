package net.englab.contextsearcher.subtitles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.exceptions.HighlightingException;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that performs text highlighting in subtitles.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubtitleHighlighter {

    /**
     * Locates the highlighted text from Elasticsearch in the subtitles and then highlight it.
     *
     * @param highlightedParts  the highlighted text from Elasticsearch
     * @param subtitleEntries   a part of subtitle entries where the text needs to be highlighted.
     *                          This parameter will be updated by the method.
     * @throws HighlightingException if an unexpected error occurs during highlighting
     */
    public static void highlight(String[] highlightedParts, List<SubtitleEntry> subtitleEntries) {
        String highlightedSentence = String.join("", highlightedParts);

        String subtitleText = subtitleEntries.stream()
                .map(entry -> entry.getText().get(0))
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" "));

        int sentencePosition = subtitleText.indexOf(highlightedSentence);
        if (sentencePosition == -1) {
            throw new HighlightingException("Cannot find the highlighted sentence in the subtitles");
        }

        int partIndex = 0;
        int endPosition = sentencePosition + highlightedParts[0].length();

        for (int entryIndex = 0; entryIndex < subtitleEntries.size(); entryIndex++) {
            SubtitleEntry currentEntry = subtitleEntries.get(entryIndex);
            String entryText = currentEntry.getText().get(0);

            // there's no need to process entries with no text
            if (entryText.isEmpty()) {
                continue;
            }

            // skip subtitle entries until we find the one where we have the highlighted word
            if (endPosition >= entryText.length()) {
                endPosition -= entryText.length() + 1; // length + a space character
                continue;
            }

            List<String> highlightedEntryText = new ArrayList<>();

            // try to split the current entry text
            int startPosition = 0;
            for (; partIndex < highlightedParts.length - 1 && endPosition <= entryText.length(); partIndex++) {
                String substring = entryText.substring(startPosition, endPosition);
                highlightedEntryText.add(substring);
                startPosition = endPosition;
                endPosition += highlightedParts[partIndex + 1].length();
            }

            // add the tail
            String lastPart = entryText.substring(startPosition);
            if (!lastPart.isBlank()) {
                highlightedEntryText.add(lastPart);
            }

            endPosition -= entryText.length() + 1;

            currentEntry.setText(highlightedEntryText);
        }
    }
}
