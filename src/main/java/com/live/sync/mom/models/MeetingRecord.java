package com.live.sync.mom.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MeetingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meetingId;
    private String hostName;
    private LocalDateTime dateTime;
    private int duration; // in minutes
    private String title;

    @Lob
    private String summary;

    private String transcriptPath;
    private String summaryPath;
}

