package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.FriendshipRequestAcceptResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.security.UserPrincipal;

public interface FriendshipService {
    FriendshipRequestResponseDto sendFriendshipRequest(Long addresseeId, UserPrincipal requester);

    FriendshipRequestAcceptResponseDto acceptFriendshipRequest(Long requestId, UserPrincipal requester);
}
