package com.zack.friendshub.dto.response.friendship;

import com.zack.friendshub.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendshipRequestDecisionResponseDto(
        Long id,
        Long requesterId,
        Long addresseeId,
        FriendshipStatus status,
        LocalDateTime acceptedAt
) {
}
