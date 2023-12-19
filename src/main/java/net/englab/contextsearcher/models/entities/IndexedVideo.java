package net.englab.contextsearcher.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.subtitles.SubtitleEntry;
import org.hibernate.annotations.Immutable;

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
     * We need it to make the search work during full reindexing.
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
     * A list representing the subtitles of the video.
     * This field is marked as immutable to optimise performance.
     * If the field was mutable, Hibernate would convert it again to make a new copy,
     * which would lead to poor performance because the list of subtitles can be very long.
     */
    @Immutable
    @Convert(converter = SubtitleConverter.class)
    private List<SubtitleEntry> subtitles;
}
