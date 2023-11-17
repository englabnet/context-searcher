package net.englab.contextsearcher.subtitles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubtitleHighlighter {

    /**
     * This method modifies subtitles to highlight the text.
     *
     * @param sentence the sentence from elastic
     * @param parts the highlighted text (even-numbered elements are highlighted)
     * @param blocks the subtitle blocks where the sentence appears. This param will be modified.
     */
    public static void highlight(String sentence, String[] parts, List<SubtitleEntry> blocks) {
        String originalText = blocks.stream()
                .map(block -> block.getText().get(0).trim())
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" "));

        int offset = originalText.indexOf(sentence);
        if (offset == -1) {
            throw new RuntimeException("Cannot find the highlighted text in the subtitles.");
        }

        int p = 0;
        int endIndex = offset + parts[p].length();
        for (SubtitleEntry block : blocks) {
            String line = block.getText().get(0);

            // we want to skip any empty lines as they break highlighting
            if (line.isEmpty()) continue;

            // skip lines until we don't find the one where the text is highlighted
            if (endIndex >= line.length()) {
                endIndex -= line.length() + 1; // length + a space character
                continue;
            }

            List<String> result = new ArrayList<>();
            int beginIndex = 0;
            for (; p < parts.length - 1 && endIndex <= line.length(); p++) {
                result.add(line.substring(beginIndex, endIndex));
                beginIndex = endIndex;
                endIndex += parts[p + 1].length();
            }
            String lastPart = line.substring(beginIndex);
            if (!lastPart.isBlank()) {
                result.add(lastPart);
            }

            endIndex -= line.length() + 1;

            block.setText(result);
        }
    }
}
