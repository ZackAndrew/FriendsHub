package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.FriendDto;
import com.zack.friendshub.dto.response.FriendshipRequestDecisionResponseDto;
import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.util.List;

public interface FriendshipService {
    FriendshipRequestResponseDto sendFriendshipRequest(Long addresseeId, UserPrincipal requester);

    FriendshipRequestDecisionResponseDto acceptFriendshipRequest(Long requestId, UserPrincipal requester);

    FriendshipRequestDecisionResponseDto declineFriendshipRequest(Long requestId, UserPrincipal currentUser);

    List<FriendDto> getAllFriends(UserPrincipal currentUser);
}
