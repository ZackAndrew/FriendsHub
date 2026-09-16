package com.zack.friendshub.service;

import com.zack.friendshub.mapper.MeetingMapper;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.MeetingRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.impl.MeetingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceImplTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private FriendshipRepo friendshipRepo;
    @Mock
    private AvailabilityRepo availabilityRepo;
    @Mock
    private MeetingRepo meetingRepo;
    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    public void sendMeetingRequest_ThrowsSelfMeetingRequestException_WhenSendingToSelf(){
    }
}
