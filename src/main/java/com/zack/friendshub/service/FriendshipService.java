package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;

public interface FriendshipService {
    public FriendshipRequestResponseDto sendFriendshipRequest(Long addresseeId);
}
