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
import java.util.Optional;

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

        Optional<Friendship> existingFriendshipOpt = friendshipRepo.findFriendshipBetween(
                requester.getId(),
                addressee.getId()
        );

        if (existingFriendshipOpt.isPresent()) {
            Friendship existing = existingFriendshipOpt.get();

            if (existing.getStatus() == FriendshipStatus.ACCEPTED || existing.getStatus() == FriendshipStatus.PENDING) {
                throw new FriendshipRequestAlreadyExistsException(
                        "Friendship active or pending between users %d and %d".formatted(requester.getId(), addressee.getId())
                );
            }
            if (existing.getStatus() == FriendshipStatus.DECLINED) {
                if (existing.getAddressee().getId().equals(requester.getId())) {
                    existing.setRequester(requester);
                    existing.setAddressee(addressee);
                    existing.setStatus(FriendshipStatus.PENDING);
                    existing.setCreatedAt(LocalDateTime.now());

                    Friendship saved = friendshipRepo.save(existing);
                    return friendshipMapper.toResponse(saved);
                } else {
                    throw new FriendshipRequestAlreadyExistsException("Your previous request was declined.");
                }
            }
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

    @Override
    public List<FriendshipRequestResponseDto> getAllPendingFriendshipRequests(UserPrincipal currentUser) {
        Long userId = currentUser.getId();

        return friendshipRepo.findAllByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(friendshipMapper::toResponse)
                .toList();
    }

    @Override
    public FriendshipRequestResponseDto removeFriend(Long requestId, UserPrincipal currentUser) {

        Friendship friendship = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship request not found"));

        long currentUserId = currentUser.getId();

        boolean isRequester = friendship.getRequester().getId().equals(currentUserId);
        boolean isAddressee = friendship.getAddressee().getId().equals(currentUserId);

        if (!isRequester && !isAddressee) {
            throw new AccessDeniedException("Not authorized");
        }

        if (friendship.getStatus() == FriendshipStatus.DECLINED && isRequester) {
            throw new AccessDeniedException("You cannot remove a declined friend request.");
        }

        if (friendship.getStatus() == FriendshipStatus.PENDING && isRequester) {
            friendshipRepo.delete(friendship);
            return friendshipMapper.toResponse(friendship);
        }

        if (isRequester) {
            User temp = friendship.getRequester();
            friendship.setRequester(friendship.getAddressee());
            friendship.setAddressee(temp);
        }

        friendship.setStatus(FriendshipStatus.DECLINED);

        return friendshipMapper.toResponse(friendship);
    }
}
