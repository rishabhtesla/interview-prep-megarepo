package com.interviewprep.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class StudyProgress {
    @Id
    @Column(length = 80)
    private String topicId;
    @Column(nullable = false)
    private boolean completed;
    @Column(nullable = false, length = 500)
    private String note;
    @Version
    private Long version;

    protected StudyProgress() {}

    public StudyProgress(String topicId) {
        this.topicId = topicId;
        this.note = "";
    }

    public void replace(ProgressRequest request) {
        completed = request.completed();
        note = request.note() == null ? "" : request.note();
    }

    public ProgressResponse toResponse() {
        return new ProgressResponse(topicId, completed, note);
    }
}
