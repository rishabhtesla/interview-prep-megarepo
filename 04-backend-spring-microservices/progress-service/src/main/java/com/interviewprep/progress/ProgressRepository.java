package com.interviewprep.progress;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<StudyProgress, String> {
    List<StudyProgress> findAllByOrderByTopicIdAsc();
}
