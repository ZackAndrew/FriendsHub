package com.zack.friendshub.repository;

import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.model.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepo extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findAllByMeetingId(Long meetingId);

    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long participantId);

    List<MeetingParticipant> findAllByUserIdAndStatus(Long userId, MeetingStatus status);
}
