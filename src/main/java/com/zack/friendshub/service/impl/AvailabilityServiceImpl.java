package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.availability.AvailabilityResponseDto;
import com.zack.friendshub.dto.response.availability.CommonSlotResponseDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!requesterId.equals(friendId)) {
            if (!friendshipRepo.existsBetweenUsers(friendId, requesterId)) {
                throw new EntityNotFoundException("You are not friends");
            }
        }

        List<Availability> availabilities = availabilityRepo.findAllByUserId(friendId);

        return availabilities.stream()
                .map(availabilityMapper::toResponse)
                .toList();
    }

    @Override
    public List<CommonSlotResponseDto> findCommonSlots(
            LocalDateTime from,
            LocalDateTime to,
            UserPrincipal currentUser
    ) {
        Long currentUserId = currentUser.getId();

        List<Availability> mySlots = availabilityRepo.findAllByUserIdAndDateRange(currentUserId, from, to);
        if (mySlots.isEmpty()) return List.of();

        List<Long> friendIds = friendshipRepo.findAllFriendIdsByUserId(currentUserId);
        if (friendIds.isEmpty()) return List.of();

        List<Availability> allFriendsSlots = availabilityRepo.findAllByUserIdsAndDateRange(friendIds, from, to);

        Map<User, List<Availability>> slotsByFriend = allFriendsSlots.stream().
                collect(Collectors.groupingBy(Availability::getUser));

        List<CommonSlotResponseDto> commonSlots = new ArrayList<>();

        for (Map.Entry<User, List<Availability>> entry : slotsByFriend.entrySet()) {
            User friend = entry.getKey();
            List<Availability> currentFriendSlots = entry.getValue();

            for (Availability mySlot : mySlots) {
                for (Availability friendSlot : currentFriendSlots) {

                    LocalDateTime overlapStart = mySlot.getStartTime().isAfter(friendSlot.getStartTime())
                            ? mySlot.getStartTime() : friendSlot.getStartTime();

                    LocalDateTime overlapEnd = mySlot.getEndTime().isBefore(friendSlot.getEndTime())
                            ? mySlot.getEndTime() : friendSlot.getEndTime();

                    if (overlapStart.isBefore(overlapEnd)) {
                        LocalDateTime finalStart = overlapStart.isAfter(from) ? overlapStart : from;
                        LocalDateTime finalEnd = overlapEnd.isBefore(to) ? overlapEnd : to;

                        if (finalStart.isBefore(finalEnd)) {
                            commonSlots.add(new CommonSlotResponseDto(
                                    finalStart,
                                    finalEnd,
                                    friend.getUsername()
                            ));
                        }
                    }
                }
            }
        }
        commonSlots.sort(Comparator.comparing(CommonSlotResponseDto::startTime));

        return commonSlots;
    }
}
