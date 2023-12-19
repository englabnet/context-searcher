package net.englab.contextsearcher.elastic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A collection of constants that are related to the video index.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoIndexProperties {
    public static final String VIDEO_INDEX_NAME = "videos";

    public static final String YOUTUBE_VIDEO_ID = "youtube_video_id";
    public static final String ENGLISH_VARIETY = "variety";
    public static final String SENTENCE = "sentence";
    public static final String SENTENCE_POSITION = "sentence_position";
    public static final String SENTENCE_RANGE_MAP = "sentence_range_map";
}
