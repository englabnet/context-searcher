package net.englab.contextsearcher.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.englab.contextsearcher.models.common.EnglishVariety;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String videoId; // TODO: rename it to youtubeVideoId
    @Enumerated(STRING)
    private EnglishVariety variety;
    private String srt;
}
