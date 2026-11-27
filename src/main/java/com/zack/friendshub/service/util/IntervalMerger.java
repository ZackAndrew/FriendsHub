package com.zack.friendshub.service.util;

import com.zack.friendshub.dto.TimeSlot;
import com.zack.friendshub.dto.response.ScoredTimeSlot;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class IntervalMerger {

    public static final int STEP_MINUTES = 30;

    public List<ScoredTimeSlot> findTopScoredSlots(Map<Long, List<TimeSlot>> userBusySlots,
                                                   LocalDateTime windowStart,
                                                   LocalDateTime windowEnd,
                                                   int durationMinutes) {
        if (userBusySlots == null || userBusySlots.isEmpty() || durationMinutes <= 0) {
            return new ArrayList<>();
        }

        List<ScoredTimeSlot> candidates = new ArrayList<>();
        List<Long> allUserIds = new ArrayList<>(userBusySlots.keySet());
        int totalUsers = userBusySlots.size();
        long maxPossibleFreeMinutes = (long) totalUsers * durationMinutes;

        LocalDateTime currentStart = windowStart;

        while (!currentStart.plusMinutes(durationMinutes).isAfter(windowEnd)) {
            LocalDateTime currentEnd = currentStart.plusMinutes(durationMinutes);

            long totalBusyMinutes = 0;
            List<Long> fullyAvailableUsers = new ArrayList<>();

            for (Map.Entry<Long, List<TimeSlot>> entry : userBusySlots.entrySet()) {
                Long userId = entry.getKey();
                long busyMinutes = calculateUserBusyMinutes(entry.getValue(), currentStart, currentEnd);
                totalBusyMinutes += busyMinutes;

                if (busyMinutes == 0) {
                    fullyAvailableUsers.add(userId);
                }
            }

            long totalFreeMinutes = Math.max(0, maxPossibleFreeMinutes - totalBusyMinutes);
            int score = maxPossibleFreeMinutes > 0
                    ? (int) Math.round((double) totalFreeMinutes * 100.0 / maxPossibleFreeMinutes)
                    : 100;

            // Include candidate if there is at least some free time (> 0%)
            if (score > 0) {
                List<Long> participantsInSlot = fullyAvailableUsers.isEmpty() ? allUserIds : fullyAvailableUsers;
                candidates.add(new ScoredTimeSlot(currentStart, currentEnd, participantsInSlot, score));
            }

            currentStart = currentStart.plusMinutes(STEP_MINUTES);
        }

        return sortCandidates(candidates, durationMinutes);
    }

    private long calculateUserBusyMinutes(List<TimeSlot> busySlots, LocalDateTime start, LocalDateTime end) {
        if (busySlots == null || busySlots.isEmpty()) return 0;

        List<TimeSlot> clipped = new ArrayList<>();
        for (TimeSlot slot : busySlots) {
            if (slot.start().isBefore(end) && slot.end().isAfter(start)) {
                LocalDateTime s = slot.start().isBefore(start) ? start : slot.start();
                LocalDateTime e = slot.end().isAfter(end) ? end : slot.end();
                if (s.isBefore(e)) {
                    clipped.add(new TimeSlot(s, e));
                }
            }
        }
        if (clipped.isEmpty()) return 0;

        // Sort and merge overlapping intervals to avoid double-counting busy minutes
        clipped.sort((a, b) -> a.start().compareTo(b.start()));
        long totalMinutes = 0;
        LocalDateTime curS = clipped.get(0).start();
        LocalDateTime curE = clipped.get(0).end();

        for (int i = 1; i < clipped.size(); i++) {
            TimeSlot next = clipped.get(i);
            if (!next.start().isAfter(curE)) {
                if (next.end().isAfter(curE)) {
                    curE = next.end();
                }
            } else {
                totalMinutes += Duration.between(curS, curE).toMinutes();
                curS = next.start();
                curE = next.end();
            }
        }
        totalMinutes += Duration.between(curS, curE).toMinutes();
        return totalMinutes;
    }

    private List<ScoredTimeSlot> sortCandidates(List<ScoredTimeSlot> candidates, int durationMinutes) {
        if (candidates == null || candidates.isEmpty()) return new ArrayList<>();

        // Sort descending by score (least busy first), then ascending by start time
        candidates.sort((a, b) -> {
            int scoreCompare = Integer.compare(b.score(), a.score());
            if (scoreCompare != 0) return scoreCompare;
            return a.start().compareTo(b.start());
        });

        List<ScoredTimeSlot> top3 = new ArrayList<>();
        long minGapMinutes = Math.max(120, Math.min(240, durationMinutes));

        // First pass: pick diverse slots
        for (ScoredTimeSlot candidate : candidates) {
            if (top3.size() == 3) break;

            boolean isDiverse = true;
            for (ScoredTimeSlot selected : top3) {
                long minutesBetween = Math.abs(Duration.between(candidate.start(), selected.start()).toMinutes());
                if (minutesBetween < minGapMinutes) {
                    isDiverse = false;
                    break;
                }
            }

            if (isDiverse) {
                top3.add(candidate);
            }
        }

        // Second pass: if fewer than 3 diverse slots found, backfill with best available
        if (top3.size() < 3) {
            for (ScoredTimeSlot candidate : candidates) {
                if (top3.size() == 3) break;
                boolean alreadyAdded = top3.stream().anyMatch(s -> s.start().equals(candidate.start()));
                if (!alreadyAdded) {
                    top3.add(candidate);
                }
            }
        }

        return top3;
    }
}
