package com.zack.friendshub.dto.request;

import com.zack.friendshub.dto.response.ScoredTimeSlot;

import java.util.List;

public record MeetingProposalDto(
        List<String> participantNames,
        String title,
        List<ScoredTimeSlot> proposedOptions
) {
}
