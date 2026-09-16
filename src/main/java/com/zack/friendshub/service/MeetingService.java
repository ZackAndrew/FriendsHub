package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.util.List;

public interface MeetingService {
    MeetingResponseDto sendMeetingRequest(MeetingRequestDto dto, UserPrincipal currentUser);

    MeetingResponseDto acceptMeetingRequest(Long meetingId, UserPrincipal currentUser);

    MeetingResponseDto declineMeetingRequest(Long meetingId, UserPrincipal currentUser);

    MeetingResponseDto cancelMeetingRequest(Long meetingId, UserPrincipal currentUser);

    List<MeetingResponseDto> getPendingMeetings(UserPrincipal currentUser);

    List<MeetingResponseDto> getUserMeetings(UserPrincipal currentUser);

    MeetingResponseDto getMeetingById(Long meetingId, UserPrincipal currentUser);

    List<MeetingResponseDto> getOutgoingMeetings(UserPrincipal currentUser);
}
