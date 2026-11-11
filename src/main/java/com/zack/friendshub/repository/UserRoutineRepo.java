package com.zack.friendshub.repository;

import com.zack.friendshub.model.UserRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoutineRepo extends JpaRepository<UserRoutine, Long> {
    void deleteAllByUser_Id(Long userId);

    List<UserRoutine> findAllByUserId(Long userId);
}
