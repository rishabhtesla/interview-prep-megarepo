package com.interviewprep.catalog;

import java.util.List;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicService service;

    public TopicController(TopicService service) {
        this.service = service;
    }

    @GetMapping
    public List<TopicResponse> list(@RequestParam(required = false)
            @Pattern(regexp = "[a-z-]{1,40}") String track) {
        return service.list(track);
    }

    @GetMapping("/{id}")
    public TopicResponse get(@PathVariable @Pattern(regexp = "[a-z0-9-]{1,80}") String id) {
        return service.get(id);
    }
}
