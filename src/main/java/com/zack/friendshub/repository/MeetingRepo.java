package com.zack.friendshub.repository;

import com.zack.friendshub.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepo extends JpaRepository<Meeting, Long> {
    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.participants p WHERE m.organizerId = :userId OR p.user.id = :userId")
    List<Meeting> findAllUserMeetings(@Param("userId") Long userId);

    List<Meeting> findAllByOrganizerId(Long organizerId);
}
