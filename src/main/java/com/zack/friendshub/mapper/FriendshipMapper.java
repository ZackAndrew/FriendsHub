package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.model.Friendship;
import org.springframework.stereotype.Component;

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
}
