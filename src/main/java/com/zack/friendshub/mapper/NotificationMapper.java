package com.zack.friendshub.mapper;

import com.zack.friendshub.dto.response.NotificationResponseDto;
import com.zack.friendshub.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDto toDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getRelatedMeetingId(),
                notification.getCreatedAt()
        );
    }
}
