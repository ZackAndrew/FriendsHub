package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.enums.FriendshipStatus;
import com.zack.friendshub.exception.FriendshipRequestAlreadyExistsException;
import com.zack.friendshub.exception.SelfFriendshipRequestException;
import com.zack.friendshub.mapper.FriendshipMapper;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepo friendshipRepo;
    private final UserRepo userRepo;
    private final FriendshipMapper friendshipMapper;

    @Override
    public FriendshipRequestResponseDto sendFriendshipRequest(Long addresseeId) {

        String currentUsername = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User requester = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (requester.getId().equals(addresseeId)) {
            throw new SelfFriendshipRequestException(
                    "User cannot send friendship request to himself"
            );
        }

        User addressee = userRepo.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = friendshipRepo.existsBetweenUsers(
                requester.getId(),
                addressee.getId()
        );

        if (exists) {
            throw new FriendshipRequestAlreadyExistsException(
                    "Friendship already exists between users " + requester.getId() + " and " + addressee.getId()
            );
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        Friendship saved = friendshipRepo.save(friendship);

        return friendshipMapper.toResponse(saved);
    }
}
