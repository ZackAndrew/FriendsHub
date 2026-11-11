package com.zack.friendshub.repository;

import com.zack.friendshub.model.MeetingOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingOptionRepo extends JpaRepository<MeetingOption,Long> {
}
