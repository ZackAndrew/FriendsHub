package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.FriendshipRequestDecisionResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.model.Friendship;
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
}
