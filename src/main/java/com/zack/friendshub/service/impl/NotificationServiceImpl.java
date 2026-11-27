package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.response.NotificationResponseDto;
import com.zack.friendshub.exception.BadRequestException;
import com.zack.friendshub.exception.NotFoundException;
import com.zack.friendshub.exception.SelfMeetingRequestException;
import com.zack.friendshub.mapper.NotificationMapper;
import com.zack.friendshub.model.Notification;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.NotificationRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponseDto> getUserNotifications(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepo.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepo.findAllByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepo.countAllByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("It's not your notification");
        }

        notification.setIsRead(true);

        notificationRepo.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepo.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepo.saveAll(notifications);
    }

    @Override
    @Transactional
    public NotificationResponseDto createNotification(Long recipientId, String message, Long relatedMeetingId) {
        User recipient = userRepo.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Notification notification = Notification.builder()
                .user(recipient)
                .message(message)
                .isRead(false)
                .relatedMeetingId(relatedMeetingId)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationMapper.toDto(notificationRepo.save(notification));
    }
}
