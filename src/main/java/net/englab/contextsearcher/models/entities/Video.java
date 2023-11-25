package net.englab.contextsearcher.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.englab.contextsearcher.models.common.EnglishVariety;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents a video entity stored in the database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Video {

    /**
     * The unique identifier of the video.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * The YouTube video ID.
     */
    private String videoId; // TODO: rename it to youtubeVideoId

    /**
     * The variety of English that is used in the video.
     */
    @Enumerated(STRING)
    private EnglishVariety variety;

    /**
     * The subtitles of the video in the SRT format.
     */
    private String srt;
}
