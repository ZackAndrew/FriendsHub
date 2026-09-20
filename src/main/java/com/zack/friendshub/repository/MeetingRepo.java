package com.zack.friendshub.repository;

import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepo extends JpaRepository<Meeting, Long> {
    @Query("SELECT m FROM Meeting m WHERE m.organizerId = :userId OR m.participantId = :userId")
    List<Meeting> findAllUserMeetings(@Param("userId") Long userId);

    List<Meeting> findAllByParticipantIdAndStatus(Long participantId, MeetingStatus status);

    List<Meeting> findAllByOrganizerIdAndStatus(Long organizerId, MeetingStatus status);
}
