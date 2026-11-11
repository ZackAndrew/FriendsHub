package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.MeetingProposalDto;
import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.dto.request.MeetingVoteDto;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;

import java.util.List;

public interface MeetingService {
    MeetingResponseDto sendMeetingRequest(MeetingRequestDto dto, UserPrincipal currentUser);

    MeetingResponseDto acceptMeetingRequest(Long meetingId, UserPrincipal currentUser);

    MeetingResponseDto declineMeetingRequest(Long meetingId, UserPrincipal currentUser);

    void cancelMeetingRequest(Long meetingId, UserPrincipal currentUser);

    List<MeetingResponseDto> getPendingMeetings(UserPrincipal currentUser);

    List<MeetingResponseDto> getUserMeetings(UserPrincipal currentUser);

    MeetingResponseDto getMeetingById(Long meetingId, UserPrincipal currentUser);

    List<MeetingResponseDto> getOutgoingMeetings(UserPrincipal currentUser);

    MeetingResponseDto proposeSmartMeeting(MeetingProposalDto dto, UserPrincipal currentUser);

    MeetingResponseDto voteForOptions(Long meetingId, MeetingVoteDto dto, UserPrincipal currentUser);

    MeetingResponseDto finalizeSmartMeeting(Long meetingId, Long winningOptionId, UserPrincipal currentUser);
}
