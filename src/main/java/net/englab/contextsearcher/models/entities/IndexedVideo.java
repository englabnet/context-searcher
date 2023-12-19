package net.englab.contextsearcher.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;

import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents an indexed video entity stored in the database.
 * It's used to store video data in a more convenient format for runtime.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class IndexedVideo {

    /**
     * The unique identifier of the video.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * The name of the Elasticsearch index where this video is stored.
     */
    private String indexName;

    /**
     * The YouTube video ID.
     */
    private String youtubeVideoId;

    /**
     * The variety of English that is used in the video.
     */
    @Enumerated(STRING)
    private EnglishVariety variety;

    /**
     * The subtitles of the video in the SRT format.
     */
    @Convert(converter = SubtitleConverter.class)
    private List<SubtitleEntry> subtitles;
}
