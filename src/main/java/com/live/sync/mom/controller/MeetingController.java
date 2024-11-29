package com.live.sync.mom.controller;

import com.live.sync.mom.models.MeetingRecord;
import com.live.sync.mom.services.FileStorageService;
import com.live.sync.mom.services.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    @Autowired
    private MeetingService meetingService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public MeetingRecord handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestBody Map<String, Object> requestData) {

        // Extracting data from the JSON body
        String meetingId = (String) requestData.get("meetingId");
        String hostName = (String) requestData.get("hostName");
        String dateTime = (String) requestData.get("dateTime");
        int duration = (int) requestData.get("duration");
        String title = (String) requestData.get("title");

        // Store file
        String transcriptPath = fileStorageService.storeFile(file, "transcripts");

        // Placeholder for ML-based summary
        String summary = "This is a placeholder summary.";

        // Create and save meeting record
        MeetingRecord record = new MeetingRecord();
        record.setMeetingId(meetingId);
        record.setHostName(hostName);
        record.setDateTime(LocalDateTime.parse(dateTime));
        record.setDuration(duration);
        record.setTitle(title);
        record.setTranscriptPath(transcriptPath);
        record.setSummary(summary);

        return meetingService.saveMeetingRecord(record);
    }

    @GetMapping
    public List<MeetingRecord> getAllMeetings() {
        return meetingService.getAllMeetingRecords();
    }
}
