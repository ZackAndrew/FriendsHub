package com.zack.friendshub.service;

import com.zack.friendshub.dto.response.NotificationResponseDto;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDto> getUserNotifications(Long userId, boolean unreadOnly);

    Long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    NotificationResponseDto createNotification(Long recipientId, String message, Long relatedMeetingId);
}
