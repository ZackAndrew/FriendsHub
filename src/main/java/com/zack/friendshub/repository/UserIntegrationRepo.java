package com.zack.friendshub.repository;

import com.zack.friendshub.model.UserIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIntegrationRepo extends JpaRepository<UserIntegration, Long> {

    Optional<UserIntegration> findByUserIdAndProvider(Long userId, String provider);
}
