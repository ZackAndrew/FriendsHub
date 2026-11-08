package com.zack.friendshub.service.impl;

import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final UserRepo userRepo;
    private final FriendshipRepo friendshipRepo;


}
