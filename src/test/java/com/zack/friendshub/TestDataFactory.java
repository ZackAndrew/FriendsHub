package com.zack.friendshub;

import com.zack.friendshub.enums.FriendshipStatus;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.Availability;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestDataFactory {

    @Autowired
    UserRepo userRepo;

    @Autowired
    FriendshipRepo friendshipRepo;

    @Autowired
    AvailabilityRepo availabilityRepo;

    public User saveUser(String username) {
        User user = User.builder()
                .username(username)
                .name(username)
                .role(Role.USER)
                .status(UserStatus.ACTIVATED)
                .dateOfRegistration(LocalDateTime.now())
                .email(username + "@test.com")
                .passwordHash("password")
                .build();
        return userRepo.save(user);
    }

    public void saveFriendship(User requester, User addressee, FriendshipStatus status) {
        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepo.save(friendship);
    }

    public void saveAvailability(User user, LocalDateTime start, LocalDateTime end) {
        Availability availability = Availability.builder()
                .user(user)
                .startTime(start)
                .endTime(end)
                .build();
        availabilityRepo.save(availability);
    }
}