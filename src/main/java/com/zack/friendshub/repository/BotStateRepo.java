package com.zack.friendshub.repository;

import com.zack.friendshub.model.BotStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotStateRepo extends JpaRepository<BotStateEntity, Long> {
}
