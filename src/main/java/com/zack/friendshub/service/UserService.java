package com.zack.friendshub.service;

import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    List<User> getAllUsers();

    User updateUserStatus(Long id, UserStatus status);

    void deleteUserById(Long id);
}
