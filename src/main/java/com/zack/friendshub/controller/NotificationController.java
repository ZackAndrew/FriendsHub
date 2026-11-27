package com.zack.friendshub.controller;

import com.zack.friendshub.dto.response.NotificationResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Operations related with notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        List<NotificationResponseDto> response = notificationService.getUserNotifications(currentUser.getId(), unreadOnly);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        Long response = notificationService.getUnreadCount(currentUser.getId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAsRead(notificationId, currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
