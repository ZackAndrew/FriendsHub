package com.zack.friendshub.service;

import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> getUserById(Long id);

    Optional<User> getUserByUsername(String username);

    List<User> getAllUsers();

    User updateUserStatus(Long id, UserStatus status);

    void deleteUserById(Long id);
}
