package com.zack.friendshub.service.impl;

import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepo.findById(userId);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));
        user.setStatus(status);
        userRepo.save(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));
        userRepo.delete(user);
    }
}
