package net.englab.contextsearcher.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.dto.SubtitleBlock;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.repositories.VideoRepository;
import net.englab.contextsearcher.utils.SrtSubtitles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VideoStorage {

    private final VideoRepository videoRepository;

    public Long save(Video video) {
        return videoRepository.save(video).getId();
    }

    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    public Optional<Video> findByVideoId(String videoId) {
        return videoRepository.findByVideoId(videoId);
    }

    @Transactional
    public void deleteById(Long id) {
        videoRepository.deleteById(id);
    }

    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    public Page<Video> findAll(Specification<Video> specification, Pageable pageable) {
        return videoRepository.findAll(specification, pageable);
    }

    public List<SubtitleBlock> findSubtitlesByVideoId(String videoId) {
        // TODO: These stream transformations make the search two times slower.
        //  I can optimise it by making it part of the indexing.
        return videoRepository.findByVideoId(videoId).stream()
                .map(Video::getSrt)
                .map(SrtSubtitles::new)
                .flatMap(SrtSubtitles::stream)
                .map(b -> new SubtitleBlock(
                        b.timeFrame().startTime(),
                        b.timeFrame().endTime(),
                        List.of(String.join(" ", b.text())))
                ).toList();
    }
}
