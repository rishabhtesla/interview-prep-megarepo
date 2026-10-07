package com.interviewprep.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, String> {
    List<Topic> findAllByOrderByIdAsc();
    List<Topic> findByTrackOrderByIdAsc(String track);
}
