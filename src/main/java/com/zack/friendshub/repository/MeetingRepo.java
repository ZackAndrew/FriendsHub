package com.zack.friendshub.repository;

import com.zack.friendshub.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepo extends JpaRepository<Meeting, Long> {
}
