package com.zack.friendshub.repository;

import com.zack.friendshub.model.MeetingVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingVoteRepo extends JpaRepository<MeetingVote, Long> {
}
