package com.citi.meetingsummarizer.repository;

import com.citi.meetingsummarizer.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {
    // Custom query methods
    List<Meeting> findByMeetingDateBetween(LocalDateTime start, LocalDateTime end);
    List<Meeting> findByTitleContainingIgnoreCase(String title);
}