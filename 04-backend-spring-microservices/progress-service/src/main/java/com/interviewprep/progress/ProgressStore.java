package com.interviewprep.progress;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressStore {
    private final ProgressRepository repository;

    public ProgressStore(ProgressRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> list() {
        return repository.findAllByOrderByTopicIdAsc().stream().map(StudyProgress::toResponse).toList();
    }

    @Transactional
    public ProgressResponse replace(String topicId, ProgressRequest request) {
        var progress = repository.findById(topicId).orElseGet(() -> new StudyProgress(topicId));
        progress.replace(request);
        return repository.saveAndFlush(progress).toResponse();
    }
}
