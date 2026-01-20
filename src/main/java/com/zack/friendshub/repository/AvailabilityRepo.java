package com.zack.friendshub.repository;

import com.zack.friendshub.model.Availability;
import com.zack.friendshub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityRepo extends JpaRepository<Availability, Long> {

    List<Availability> findAllByUser(User user);

    List<Availability> findAllByUserId(Long userId);

    @Query("""
            SELECT COUNT(a) > 0 FROM Availability a 
            WHERE a.user.id = :userId 
            AND a.startTime < :newEnd 
            AND a.endTime > :newStart
            """)
    boolean existsOverlapping(
            @Param("userId") Long userId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );
}
