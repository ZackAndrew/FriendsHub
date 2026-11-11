package com.zack.friendshub.service.impl;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.zack.friendshub.converter.RoutineTimeConverter;
import com.zack.friendshub.dto.TimeSlot;
import com.zack.friendshub.dto.request.AvailabilityRequestDto;
import com.zack.friendshub.dto.response.ScoredTimeSlot;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.UserRoutine;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.repository.UserRoutineRepo;
import com.zack.friendshub.service.AvailabilityService;
import com.zack.friendshub.service.GoogleCalendarService;
import com.zack.friendshub.service.util.IntervalMerger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final UserRepo userRepo;
    private final UserRoutineRepo userRoutineRepo;
    private final RoutineTimeConverter routineTimeConverter;
    private final GoogleCalendarService googleCalendarService;
    private final IntervalMerger intervalMerger;

    @Override
    public List<TimeSlot> mapGoogleEventsToTimeSlots(List<Event> events, LocalDateTime windowStart, LocalDateTime windowEnd) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        if (events == null || events.isEmpty()) return timeSlots;

        for (Event event : events) {
            LocalDateTime eventStart = parseGoogleDateTime(event.getStart());
            LocalDateTime eventEnd = parseGoogleDateTime(event.getEnd());

            if (eventStart == null || eventEnd == null || !eventStart.isBefore(eventEnd)) continue;

            if (eventStart.isBefore(windowEnd) && eventEnd.isAfter(windowStart)) {

                LocalDateTime actualStart = eventStart.isBefore(windowStart) ? windowStart : eventStart;
                LocalDateTime actualEnd = eventEnd.isAfter(windowEnd) ? windowEnd : eventEnd;
                if (actualStart.isBefore(actualEnd)) {
                    timeSlots.add(new TimeSlot(actualStart, actualEnd));
                }
            }
        }

        return timeSlots;
    }

    @Override
    public List<ScoredTimeSlot> calculateCommonFreeTime(AvailabilityRequestDto request, Long currentUserId) {
        List<User> participants = userRepo.findAllByUsernameIn(request.participantNames());
        Set<Long> userIds = participants.stream().map(User::getId).collect(Collectors.toSet());

        List<TimeSlot> virtualBlockers = generateVirtualBlockers(
                request.windowStart(), request.windowEnd(),
                request.preferredDaysOfWeek(),
                request.dailyStartTime(), request.dailyEndTime()
        );

        Map<Long, List<TimeSlot>> userBusySlots = new HashMap<>();

        for (Long userId : userIds) {
            List<TimeSlot> personalBusy = new ArrayList<>(virtualBlockers);

            List<UserRoutine> routines = userRoutineRepo.findAllByUserId((userId));
            personalBusy.addAll(routineTimeConverter.convertRoutinesToTimeSlots(
                    routines, request.windowStart(), request.windowEnd()));

            List<Event> events = googleCalendarService.getUserEvents(userId, request.windowStart(), request.windowEnd());
            personalBusy.addAll(mapGoogleEventsToTimeSlots(events, request.windowStart(), request.windowEnd()));

            userBusySlots.put(userId, personalBusy);
        }

        return intervalMerger.findTopScoredSlots(
                userBusySlots,
                request.windowStart(),
                request.windowEnd(),
                request.durationMinutes()
        );
    }

    /**
     * Generates virtual time slots representing "busy" time based on the selected preset.
     * <p>
     * IMPORTANT FRONTEND CONTRACT FOR "NIGHT SHIFT" EVENTS:
     * If the user selects a time range that crosses midnight (e.g., 22:00 - 05:00),
     * the frontend MUST send BOTH days in the 'preferredDaysOfWeek' array.
     * <p>
     * Example: A party on Friday night (22:00 to 05:00 Saturday).
     * The frontend must send: preferredDaysOfWeek = [5, 6] (Friday AND Saturday).
     * If Saturday [6] is omitted, the system will block the entire Saturday,
     * and the free slot will be strictly cut off at 00:00 (midnight).
     */
    @Override
    public List<TimeSlot> generateVirtualBlockers(LocalDateTime windowStart, LocalDateTime windowEnd,
                                                  List<Integer> preferredDays,
                                                  LocalTime dailyStartTime, LocalTime dailyEndTime) {
        List<TimeSlot> blockers = new ArrayList<>();
        LocalDate currentDate = windowStart.toLocalDate();
        LocalDate endDate = windowEnd.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();

            if (preferredDays != null && !preferredDays.isEmpty() && !preferredDays.contains(dayOfWeek)) {
                blockers.add(new TimeSlot(currentDate.atStartOfDay(), currentDate.plusDays(1).atStartOfDay()));
            } else {

                if (dailyStartTime != null && dailyEndTime != null && dailyStartTime.isAfter(dailyEndTime)) {
                    blockers.add(new TimeSlot(currentDate.atTime(dailyEndTime), currentDate.atTime(dailyStartTime)));
                } else {

                    if (dailyStartTime != null && dailyStartTime.isAfter(LocalTime.MIN)) {
                        blockers.add(new TimeSlot(currentDate.atStartOfDay(), currentDate.atTime(dailyStartTime)));
                    }

                    if (dailyEndTime != null) {
                        blockers.add(new TimeSlot(currentDate.atTime(dailyEndTime), currentDate.plusDays(1).atStartOfDay()));
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        return blockers;
    }

    private LocalDateTime parseGoogleDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null || eventDateTime.isEmpty()) return null;

        if (eventDateTime.getDateTime() != null) {
            long mills = eventDateTime.getDateTime().getValue();
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault());
        } else if (eventDateTime.getDate() != null) {
            String dateString = eventDateTime.getDate().toStringRfc3339();
            return LocalDate.parse(dateString).atStartOfDay();
        }
        return null;
    }
}
