package net.englab.contextsearcher.services;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.dto.SubtitleBlock;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.repositories.VideoRepository;
import net.englab.contextsearcher.utils.SrtSubtitles;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoStorage {

    private final VideoRepository videoRepository;

    public Long save(Video video) {
        if (videoRepository.findByVideoId(video.getVideoId()).isPresent()) {
            throw new RuntimeException("The video has been indexed already!");
        }
        return videoRepository.save(video).getId();
    }

    public void deleteById(Long id) {
        videoRepository.deleteById(id);
    }

    public List<Video> findAll() {
        return videoRepository.findAll();
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
                ).collect(Collectors.toList());
    }
}
