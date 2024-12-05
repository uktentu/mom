package com.citi.meetingsummarizer.controller;

import com.citi.meetingsummarizer.model.Meeting;
import com.citi.meetingsummarizer.repository.MeetingRepository;
import com.citi.meetingsummarizer.service.FileUploadService;
import com.citi.meetingsummarizer.service.ReportGenerationService;
import com.citi.meetingsummarizer.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5001"})
public class MeetingController {
    private final FileUploadService fileUploadService;
    private final SummarizationService summarizationService;
    private final ReportGenerationService reportGenerationService;
    private final MeetingRepository meetingRepository;

    @PostMapping("/upload")
    public Mono<Meeting> uploadTranscriptAndSummarize(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("duration") Long duration
    ) {
        try {
            // Upload file
            String uploadedFilePath = fileUploadService.uploadFile(file);

            // Create meeting record
            Meeting meeting = new Meeting();
            meeting.setTitle(title);
            meeting.setMeetingDate(LocalDateTime.now());
            meeting.setDurationMinutes(duration);
            meeting.setOriginalTranscriptPath(uploadedFilePath);

            // Generate summary
            return summarizationService.generateSummary(uploadedFilePath)
                    .map(summary -> {
                        meeting.setSummarizedText(summary);
                        try {
                            // Generate editable report
                            String reportPath = reportGenerationService.generateEditableReport(meeting, summary);
                            meeting.setGeneratedReportPath(reportPath);
                        } catch (IOException e) {
                            // Log error or handle appropriately
                            meeting.setGeneratedReportPath(null);
                        }

                        // Save meeting record
                        return meetingRepository.save(meeting);
                    });
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to process transcript", e));
        }
    }

    @GetMapping
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    @GetMapping("/{id}")
    public Meeting getMeetingById(@PathVariable String id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    @PutMapping("/{id}")
    public Meeting updateMeeting(@PathVariable String id, @RequestBody Meeting updatedMeeting) {
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // Update allowed fields
        existingMeeting.setTitle(updatedMeeting.getTitle());
        existingMeeting.setLastModified(LocalDateTime.now());

        return meetingRepository.save(existingMeeting);
    }

    @DeleteMapping("/{id}")
    public void deleteMeeting(@PathVariable String id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meetingRepository.delete(meeting);
    }

    @GetMapping("/download/report/{id}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String id) throws MalformedURLException {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        if (meeting.getGeneratedReportPath() == null) {
            throw new RuntimeException("No report available for this meeting");
        }

        Path filePath = Paths.get(meeting.getGeneratedReportPath());
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}