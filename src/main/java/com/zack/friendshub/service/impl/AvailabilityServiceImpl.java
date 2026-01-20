package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.AvailabilityResponseDto;
import com.zack.friendshub.mapper.AvailabilityMapper;
import com.zack.friendshub.model.Availability;
import com.zack.friendshub.model.User;
import com.zack.friendshub.repository.AvailabilityRepo;
import com.zack.friendshub.repository.FriendshipRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.AvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepo availabilityRepo;
    private final UserRepo userRepo;
    private final AvailabilityMapper availabilityMapper;
    private final FriendshipRepo friendshipRepo;

    @Override
    public AvailabilityResponseDto saveAvailability(AvailabilityRequestDto requestDto, UserPrincipal currentUser) {
        Long userId = currentUser.getId();

        boolean overlaps = availabilityRepo.existsOverlapping(userId, requestDto.startTime(), requestDto.endTime());
        if (overlaps) {
            throw new IllegalStateException("Availability overlap");
        }
        User user = userRepo.getReferenceById(userId);

        Availability availability = Availability.builder()
                .startTime(requestDto.startTime())
                .endTime(requestDto.endTime())
                .user(user)
                .build();

        Availability result = availabilityRepo.save(availability);

        return availabilityMapper.toResponse(result);
    }

    @Override
    public List<AvailabilityResponseDto> getUserAvailability(Long friendId, UserPrincipal currentUser) {
        Long requesterId = currentUser.getId();
        if (!friendshipRepo.existsBetweenUsers(friendId, requesterId)) {
            throw new EntityNotFoundException("You are not friends");
        }
        List<Availability> availabilities = availabilityRepo.findAllByUserId(friendId);

        return availabilities.stream()
                .map(availabilityMapper::toResponse)
                .toList();
    }
}
