package com.live.sync.mom.repo;

import com.live.sync.mom.models.MeetingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRecordRepo extends JpaRepository<MeetingRecord, Long> {
}
