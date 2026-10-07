package com.interviewprep.catalog;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class TopicService {
    private final TopicRepository repository;

    public TopicService(TopicRepository repository) {
        this.repository = repository;
    }

    public List<TopicResponse> list(String track) {
        var topics = track == null ? repository.findAllByOrderByIdAsc()
                : repository.findByTrackOrderByIdAsc(track);
        return topics.stream().map(Topic::toResponse).toList();
    }

    public TopicResponse get(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found")).toResponse();
    }
}
