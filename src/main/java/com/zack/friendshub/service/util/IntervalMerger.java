package com.zack.friendshub.service.util;

import com.zack.friendshub.dto.TimeSlot;
import com.zack.friendshub.dto.response.ScoredTimeSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class IntervalMerger {

    public static final int STEP_MINUTES = 30;


    public List<ScoredTimeSlot> findTopScoredSlots(Map<Long, List<TimeSlot>> userBusySlots, LocalDateTime windowStart, LocalDateTime windowEnd, int durationMinutes) {
        List<ScoredTimeSlot> candidates = new ArrayList<>();

        LocalDateTime currentStart = windowStart;

        while (!currentStart.plusMinutes(durationMinutes).isAfter(windowEnd)) {
            LocalDateTime currentEnd = currentStart.plusMinutes(durationMinutes);
            List<Long> availableUsers = new ArrayList<>();

            for (Map.Entry<Long, List<TimeSlot>> entry : userBusySlots.entrySet()) {
                Long userId = entry.getKey();
                boolean isBusy = false;

                for (TimeSlot busy : entry.getValue()) {
                    if (busy.start().isBefore(currentEnd) && busy.end().isAfter(currentStart)) {
                        isBusy = true;
                        break;
                    }
                }
                if (!isBusy) {
                    availableUsers.add(userId);
                }
            }
            if (availableUsers.size() >= 2) {
                candidates.add(new ScoredTimeSlot(currentStart, currentEnd, availableUsers, availableUsers.size()));
            }
            currentStart = currentStart.plusMinutes(STEP_MINUTES);
        }

        return sortCandidates(candidates);
    }


    private List<ScoredTimeSlot> sortCandidates(List<ScoredTimeSlot> candidates) {

        candidates.sort((a, b) -> {
            int scoreCompare = Integer.compare(b.score(), a.score());
            if (scoreCompare != 0) return scoreCompare;
            return a.start().compareTo(b.start());
        });
        List<ScoredTimeSlot> top3 = new ArrayList<>();
        for (ScoredTimeSlot candidate : candidates) {
            if (top3.size() == 3) break;

            boolean isDiverse = true;
            for (ScoredTimeSlot selected : top3) {
                long minutesBetween = Math.abs(java.time.Duration.between(candidate.start(), selected.start()).toMinutes());
                if (minutesBetween < 240) {
                    isDiverse = false;
                    break;
                }
            }

            if (isDiverse) {
                top3.add(candidate);
            }
        }
        return top3;
    }
}
