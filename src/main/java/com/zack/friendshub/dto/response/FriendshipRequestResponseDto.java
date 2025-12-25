package com.zack.friendshub.dto.response;

import com.zack.friendshub.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendshipRequestResponseDto(
        Long id,
        Long requesterId,
        Long addresseeId,
        FriendshipStatus status,
        LocalDateTime createdAt
) {
}
