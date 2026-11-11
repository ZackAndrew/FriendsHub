package com.zack.friendshub.dto.request;

import java.util.List;

public record MeetingVoteDto(
        List<Long> selectedOptionIds
) {
}
