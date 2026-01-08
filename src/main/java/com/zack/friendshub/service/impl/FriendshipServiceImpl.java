package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.response.FriendDto;
import com.zack.friendshub.dto.response.FriendshipRequestDecisionResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.enums.FriendshipStatus;
import com.zack.friendshub.exception.FriendshipRequestAlreadyExistsException;
import com.zack.friendshub.exception.SelfFriendshipRequestException;
import com.zack.friendshub.mapper.FriendshipMapper;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.FriendshipService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepo friendshipRepo;
    private final UserRepo userRepo;
    private final FriendshipMapper friendshipMapper;

    @Override
    public FriendshipRequestResponseDto sendFriendshipRequest(
            Long addresseeId,
            UserPrincipal currentUser
    ) {

        if (currentUser.getId().equals(addresseeId)) {
            throw new SelfFriendshipRequestException(
                    "User cannot send friendship request to himself"
            );
        }

        User requester = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        User addressee = userRepo.findById(addresseeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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

    @Override
    public FriendshipRequestDecisionResponseDto acceptFriendshipRequest(Long requestId, UserPrincipal currentUser) {

        Friendship friendship = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship request not found"));

        if (!friendship.getAddressee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to accept this request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Friendship request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);

        return friendshipMapper.toDecisionResponse(friendship);
    }

    @Override
    public FriendshipRequestDecisionResponseDto declineFriendshipRequest(Long requestId, UserPrincipal currentUser) {

        Friendship friendship = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship request not found"));

        if (!currentUser.getId().equals(friendship.getAddressee().getId())) {
            throw new AccessDeniedException("You are not allowed to decline this request");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Friendship request is not pending");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendshipMapper.toDecisionResponse(friendship);
    }

    @Override
    public List<FriendDto> getAllFriends(UserPrincipal currentUser) {

        Long userId = currentUser.getId();

        return friendshipRepo.findAllFriends(userId)
                .stream()
                .map(friendship -> friendshipMapper.toFriendDto(friendship, userId))
                .toList();
    }
}
