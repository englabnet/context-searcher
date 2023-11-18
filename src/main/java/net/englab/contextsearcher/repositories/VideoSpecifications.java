package net.englab.contextsearcher.repositories;

import net.englab.contextsearcher.models.common.EnglishVariety;
import net.englab.contextsearcher.models.entities.Video;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class VideoSpecifications {
    public static Specification<Video> byId(Long id) {
        return (root, query, builder) -> {
            if (id == null) return builder.conjunction();
            return builder.equal(root.get("id"), id);
        };
    }

    public static Specification<Video> byVideoId(String videoId) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(videoId)) return builder.conjunction();
            return builder.equal(root.get("videoId"), videoId);
        };
    }

    public static Specification<Video> byVariety(EnglishVariety variety) {
        return (root, query, builder) -> {
            if (variety == null || variety == EnglishVariety.ALL) return builder.conjunction();
            return builder.equal(root.get("variety"), variety);
        };
    }
}
