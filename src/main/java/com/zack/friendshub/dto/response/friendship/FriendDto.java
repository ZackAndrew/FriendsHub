package com.zack.friendshub.dto.response.friendship;

import com.zack.friendshub.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendDto(
        Long friendshipId,
        Long userId,
        String username,
        String name,
        FriendshipStatus status,
        LocalDateTime since
) {
}
