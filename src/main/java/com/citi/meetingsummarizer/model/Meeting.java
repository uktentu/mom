package com.citi.meetingsummarizer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meeting_records")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime meetingDate;

    @Column(nullable = false)
    private Long durationMinutes;

    @Column(length = 1000)
    private String originalTranscriptPath;

    @Column(length = 5000)
    private String summarizedText;

    @Column
    private String generatedReportPath;

    @Column(nullable = false)
    private boolean isEditable = true;

    @Column
    private LocalDateTime lastModified;
}