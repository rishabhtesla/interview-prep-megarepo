package com.interviewprep.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Topic {
    @Id
    @Column(length = 80)
    private String id;
    @Column(nullable = false)
    private String track;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String summary;
    @Column(nullable = false)
    private int minutes;

    protected Topic() {}

    public Topic(String id, String track, String title, String summary, int minutes) {
        this.id = id;
        this.track = track;
        this.title = title;
        this.summary = summary;
        this.minutes = minutes;
    }

    public TopicResponse toResponse() {
        return new TopicResponse(id, track, title, summary, minutes);
    }
}
