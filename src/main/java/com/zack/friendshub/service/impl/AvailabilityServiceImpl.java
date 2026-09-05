package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.availability.AvailabilityResponseDto;
import com.zack.friendshub.dto.response.availability.GroupedCommonSlotResponseDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    public List<AvailabilityResponseDto> getUserAvailability(
            LocalDateTime from,
            LocalDateTime to,
            String friendUsername,
            UserPrincipal currentUser
    ) {
        Long requesterId = currentUser.getId();
        User friend = userRepo.findByUsername(friendUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + friendUsername));
        Long friendId = friend.getId();

        if (!requesterId.equals(friendId)) {
            if (!friendshipRepo.existsBetweenUsers(friendId, requesterId)) {
                throw new AccessDeniedException("You are not friends");
            }
        }

        LocalDateTime startTime = (from != null) ? from : LocalDateTime.now();
        LocalDateTime endTime = (to != null) ? to : LocalDateTime.now().plusMonths(1);

        List<Availability> availabilities = availabilityRepo.findAllByUserIdAndDateRange(friendId, startTime, endTime);

        return availabilities.stream()
                .map(availabilityMapper::toResponse)
                .toList();
    }

    @Override
    public List<GroupedCommonSlotResponseDto> findCommonSlots(
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

        record TimeKey(LocalDateTime start, LocalDateTime end) {
        }

        Map<TimeKey, List<String>> slotsMap = new HashMap<>();

        for (Availability mySlot : mySlots) {
            for (Availability friendSlot : allFriendsSlots) {

                LocalDateTime overlapStart = mySlot.getStartTime().isAfter(friendSlot.getStartTime())
                        ? mySlot.getStartTime() : friendSlot.getStartTime();

                LocalDateTime overlapEnd = mySlot.getEndTime().isBefore(friendSlot.getEndTime())
                        ? mySlot.getEndTime() : friendSlot.getEndTime();

                if (overlapStart.isBefore(overlapEnd)) {
                    LocalDateTime finalStart = overlapStart.isAfter(from) ? overlapStart : from;
                    LocalDateTime finalEnd = overlapEnd.isBefore(to) ? overlapEnd : to;

                    if (finalStart.isBefore(finalEnd)) {
                        TimeKey key = new TimeKey(finalStart, finalEnd);
                        slotsMap.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(friendSlot.getUser().getUsername());
                    }
                }
            }
        }
        return slotsMap.entrySet().stream()
                .map(entry -> new GroupedCommonSlotResponseDto(
                        entry.getKey().start(),
                        entry.getKey().end(),
                        entry.getValue().stream().distinct().sorted().toList()
                ))
                .sorted(Comparator.comparing(GroupedCommonSlotResponseDto::startTime))
                .toList();
    }
}
