package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.PageableDto;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.User;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    PageableDto<User> findByPage(Pageable pageable);

    User updateUserStatus(Long id, UserStatus status);

    void deleteUserById(Long id);
}
