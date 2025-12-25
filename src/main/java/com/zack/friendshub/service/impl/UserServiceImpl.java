package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.response.PageableDto;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.exception.NotFoundException;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Override
    public PageableDto<User> findByPage(Pageable pageable) {
    Page<User> users = userRepo.findAll(pageable);
        return new PageableDto<>(
                users.getContent(),
                users.getTotalElements(),
                users.getNumber(),
                users.getTotalPages()
        );
    }

    @Override
    public User updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found : " + userId));

        user.setStatus(newStatus);
        return userRepo.save(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found : " + userId));
        userRepo.delete(user);
    }
}
