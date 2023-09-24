package net.englab.contextsearcher.services;

import lombok.RequiredArgsConstructor;
import net.englab.contextsearcher.models.entities.Video;
import net.englab.contextsearcher.repositories.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
