package com.zack.friendshub.repository;

import com.zack.friendshub.model.UserRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoutineRepo extends JpaRepository<UserRoutine, Long> {
    void deleteAllByUser_Id(Long userId);
}
