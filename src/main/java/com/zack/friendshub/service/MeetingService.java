package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;

public interface MeetingService {
    MeetingResponseDto sendMeetingRequest(MeetingRequestDto dto, UserPrincipal currentUser);
}
