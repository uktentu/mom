package com.live.sync.mom.services;

import com.live.sync.mom.models.MeetingRecord;
import com.live.sync.mom.repo.MeetingRecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {
    @Autowired
    private MeetingRecordRepo repository;

    public MeetingRecord saveMeetingRecord(MeetingRecord record) {
        return repository.save(record);
    }

    public List<MeetingRecord> getAllMeetingRecords() {
        return repository.findAll();
    }
}
