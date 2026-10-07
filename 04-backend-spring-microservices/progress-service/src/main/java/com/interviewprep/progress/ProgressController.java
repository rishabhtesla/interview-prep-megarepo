package com.interviewprep.progress;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProgressResponse> list() {
        return service.list();
    }

    @PutMapping("/{topicId}")
    public ProgressResponse replace(@PathVariable @Pattern(regexp = "[a-z0-9-]{1,80}") String topicId,
                                    @Valid @RequestBody ProgressRequest request) {
        return service.replace(topicId, request);
    }
}
