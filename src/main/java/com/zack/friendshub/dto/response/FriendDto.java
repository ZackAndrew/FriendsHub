package com.zack.friendshub.dto.response;

import com.zack.friendshub.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendDto(
        Long friendshipId,
        Long userId,
        FriendshipStatus status,
        LocalDateTime createdAt
) {
}
