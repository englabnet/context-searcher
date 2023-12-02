package net.englab.contextsearcher.subtitles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.exceptions.HighlightingException;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that performs text highlighting in subtitles.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubtitleHighlighter {

    /**
     * Applies the Elasticsearch highlighting to the given subtitles.
     *
     * @param highlightedParts  the highlighted text from Elasticsearch
     * @param sentencePosition  the position where the sentence starts in the original subtitle entry
     * @param subtitleEntries   a part of subtitle entries where the text needs to be highlighted.
     *                          This parameter will be updated by the method.
     * @throws HighlightingException if an unexpected error occurs during highlighting
     */
    public static void highlight(String[] highlightedParts, int sentencePosition, List<SubtitleEntry> subtitleEntries) {
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
