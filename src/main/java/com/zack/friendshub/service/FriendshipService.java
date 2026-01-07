package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.FriendshipRequestDecisionResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.security.UserPrincipal;

public interface FriendshipService {
    FriendshipRequestResponseDto sendFriendshipRequest(Long addresseeId, UserPrincipal requester);

    FriendshipRequestDecisionResponseDto acceptFriendshipRequest(Long requestId, UserPrincipal requester);

    FriendshipRequestDecisionResponseDto declineFriendshipRequest(Long requestId, UserPrincipal currentUser);
}
