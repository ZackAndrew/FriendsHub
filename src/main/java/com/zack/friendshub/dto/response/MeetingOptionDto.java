package com.zack.friendshub.dto.response;

import java.time.LocalDateTime;

public record MeetingOptionDto(
    Long id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    int votesCount
) {}