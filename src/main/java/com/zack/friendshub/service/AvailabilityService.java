package com.zack.friendshub.service;

import com.google.api.services.calendar.model.Event;
import com.zack.friendshub.dto.TimeSlot;
import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.ScoredTimeSlot;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityService {
    List<TimeSlot> mapGoogleEventsToTimeSlots(List<Event> events, LocalDateTime windowStart, LocalDateTime windowEnd);

    List<TimeSlot> generateVirtualBlockers(LocalDateTime windowStart, LocalDateTime windowEnd, List<Integer> preferredDays, LocalTime dailyStartTime, LocalTime dailyEndTime);

    List<ScoredTimeSlot> calculateCommonFreeTime(AvailabilityRequestDto request, Long currentUserId);
}
