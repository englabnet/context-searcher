package net.englab.contextsearcher.repositories;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.entities.Video;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * The collection of specifications for filtering videos when searching in the database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoSpecifications {

    /**
     * Creates a Specification for filtering videos based on their ID.
     *
     * @param id the ID of the video to filter by. Can be null.
     * @return a Specification object to be used in a query.
     *          Returns a conjunction (no-op) if the ID is null.
     */
    public static Specification<Video> byId(Long id) {
        return (root, query, builder) -> {
            if (id == null) return builder.conjunction();
            return builder.equal(root.get("id"), id);
        };
    }

    /**
     * Creates a Specification for filtering videos based on their YouTube video ID.
     *
     * @param videoId the YouTube video ID to filter by. Can be null or empty.
     * @return a Specification object to be used in a query.
     *          Returns a conjunction (no-op) if the YouTube video ID is null or empty.
     */
    public static Specification<Video> byVideoId(String videoId) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(videoId)) return builder.conjunction();
            return builder.equal(root.get("videoId"), videoId);
        };
    }

    /**
     * Creates a Specification for filtering videos based on their variety of English.
     *
     * @param variety the variety of English to filter by. Can be null.
     * @return a Specification object to be used in a query.
     *          Returns a conjunction (no-op) if the variety is null or ALL.
     */
    public static Specification<Video> byVariety(EnglishVariety variety) {
        return (root, query, builder) -> {
            if (variety == null || variety == EnglishVariety.ALL) return builder.conjunction();
            return builder.equal(root.get("variety"), variety);
        };
    }
}
