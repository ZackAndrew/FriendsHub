package com.zack.friendshub.converter;

import com.zack.friendshub.dto.TimeSlot;
import com.zack.friendshub.model.UserRoutine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoutineTimeConverter {

    public List<TimeSlot> convertRoutinesToTimeSlots(List<UserRoutine> userRoutines,
                                                     LocalDateTime windowStart,
                                                     LocalDateTime windowEnd) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        if (userRoutines == null || userRoutines.isEmpty()) return timeSlots;

        LocalDate currentDate = windowStart.toLocalDate().minusDays(1);
        LocalDate endDate = windowEnd.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            int currentDayOfWeek = currentDate.getDayOfWeek().getValue();

            for (UserRoutine userRoutine : userRoutines) {
                if (userRoutine.getDayOfWeek().equals(currentDayOfWeek)) {

                    LocalDateTime slotStart = LocalDateTime.of(currentDate, userRoutine.getStartTime());
                    LocalDateTime slotEnd;

                    if (userRoutine.getStartTime().isAfter(userRoutine.getEndTime())) {
                        slotEnd = LocalDateTime.of(currentDate.plusDays(1), userRoutine.getEndTime());
                    } else {
                        slotEnd = LocalDateTime.of(currentDate, userRoutine.getEndTime());
                    }

                    if (slotStart.isBefore(windowEnd) && slotEnd.isAfter(windowStart)) {
                        LocalDateTime actualStartTime = slotStart.isBefore(windowStart) ? windowStart : slotStart;
                        LocalDateTime actualEndTime = slotEnd.isAfter(windowEnd) ? windowEnd : slotEnd;

                        if (actualStartTime.isBefore(actualEndTime)) {
                            timeSlots.add(new TimeSlot(actualStartTime, actualEndTime));
                        }
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        return timeSlots;
    }
}
