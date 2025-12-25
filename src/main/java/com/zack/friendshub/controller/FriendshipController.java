package com.zack.friendshub.controller;

import com.zack.friendshub.dto.response.FriendshipRequestResponseDto;
import com.zack.friendshub.service.FriendshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Friendship", description = "Operations related with friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("request/{userId}")
    public ResponseEntity<FriendshipRequestResponseDto> sendFriendRequest(@PathVariable Long userId) {
        log.info("REST request to send friend request to user ID: {}", userId);

        FriendshipRequestResponseDto response = friendshipService.sendFriendshipRequest(userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
