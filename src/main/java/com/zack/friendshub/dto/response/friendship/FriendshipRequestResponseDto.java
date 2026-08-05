package com.zack.friendshub.dto.response.friendship;

import com.zack.friendshub.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record FriendshipRequestResponseDto(
        Long id,
        Long requesterId,
        String requesterUsername,
        String requesterName,
        Long addresseeId,
        String addresseeUsername,
        String addresseeName,
        FriendshipStatus status,
        LocalDateTime createdAt
) {
}
