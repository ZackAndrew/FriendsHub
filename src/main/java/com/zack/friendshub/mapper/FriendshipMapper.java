package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.friendship.FriendDto;
import com.zack.friendshub.dto.response.friendship.FriendshipRequestDecisionResponseDto;
import com.zack.friendshub.dto.response.friendship.FriendshipRequestResponseDto;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FriendshipMapper {

    public FriendshipRequestResponseDto toResponse(Friendship friendship) {
        return new FriendshipRequestResponseDto(
                friendship.getId(),
                friendship.getRequester().getId(),
                friendship.getAddressee().getId(),
                friendship.getStatus(),
                friendship.getCreatedAt()
        );
    }

    public FriendshipRequestDecisionResponseDto toDecisionResponse(Friendship friendship) {
        return new FriendshipRequestDecisionResponseDto(
                friendship.getId(),
                friendship.getRequester().getId(),
                friendship.getAddressee().getId(),
                friendship.getStatus(),
                LocalDateTime.now()
        );
    }

    public FriendDto toFriendDto(Friendship friendship, Long userId) {
        User friend = friendship.getRequester().getId().equals(userId)
                ? friendship.getRequester()
                : friendship.getAddressee();

        return new FriendDto(
                friendship.getId(),
                friend.getId(),
                friendship.getStatus(),
                friendship.getCreatedAt()
        );
    }
}
