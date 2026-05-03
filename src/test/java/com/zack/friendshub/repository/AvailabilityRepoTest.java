package com.zack.friendshub.repository;

import com.zack.friendshub.BaseIntegrationTest;
import com.zack.friendshub.TestDataFactory;
import com.zack.friendshub.model.Availability;
import com.zack.friendshub.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class AvailabilityRepoTest extends BaseIntegrationTest {

    @Autowired
    AvailabilityRepo availabilityRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    TestDataFactory testDataFactory;

    @Test
    public void shouldDetectOverlappingAvailability() {
        User user = testDataFactory.saveUser("testUser");
        LocalDateTime start = LocalDateTime.of(2026, 6, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 6, 12, 0);

        // Availability time (10:00 - 12:00)
        testDataFactory.saveAvailability(user, start, end);

        // Full overlapping (9:30 - 13:00)
        assertThat(availabilityRepo.existsOverlapping(user.getId(), start.minusMinutes(30), end.plusHours(1))).isTrue();

        // Part overlapping (8:00 - 12:30)
        assertThat(availabilityRepo.existsOverlapping(user.getId(), start.minusHours(2), start.plusMinutes(30))).isTrue();

        // (12:00 - 14:00) should be FALSE
        assertThat(availabilityRepo.existsOverlapping(user.getId(), end, end.plusHours(2))).isFalse();

        User anotherUser = testDataFactory.saveUser("anotherUser");
        assertThat(availabilityRepo.existsOverlapping(anotherUser.getId(), start, end)).isFalse();
    }

    @Test
    public void shouldReturnAvailabilitiesWhenUserHasRecordsInDateRange() {
        User user = testDataFactory.saveUser("testUser");
        User anotherUser = testDataFactory.saveUser("anotherUser");
        LocalDateTime day1Start = LocalDateTime.of(2026, 6, 6, 10, 0);
        LocalDateTime day1End = LocalDateTime.of(2026, 6, 6, 12, 0);

        LocalDateTime day2Start = day1Start.plusDays(1);
        LocalDateTime day2End = day1End.plusDays(1);

        // Appropriate ranges
        testDataFactory.saveAvailability(user, day1Start, day1End);
        testDataFactory.saveAvailability(user, day2Start, day2End);

        // Appropriate range but wrong user
        testDataFactory.saveAvailability(anotherUser, day2Start, day2End);

        // Range is too early
        testDataFactory.saveAvailability(user, day1Start.minusDays(2), day1Start.minusDays(1));

        // Range is too late
        testDataFactory.saveAvailability(user, day2End.plusDays(1), day2End.plusDays(2));

        LocalDateTime searchStart = day1Start;
        LocalDateTime searchEnd = day2End;


        List<Availability> result = availabilityRepo.findAllByUserIdAndDateRange(user.getId(), searchStart, searchEnd);

        assertThat(result).hasSize(2).allSatisfy(availability -> assertThat(availability.getUser().getId()).isEqualTo(user.getId()));

        assertThat(result).extracting(Availability::getStartTime).containsExactlyInAnyOrder(day1Start, day2Start).isSorted();

    }

    @Test
    public void shouldReturnAvailabilitiesWhenUsersHaveRecordsInDateRange() {
        User user1 = testDataFactory.saveUser("user1");
        User user2 = testDataFactory.saveUser("user2");
        User anotherUser = testDataFactory.saveUser("anotherUser");

        List<Long> userIds = List.of(user1.getId(), user2.getId());

        LocalDateTime start = LocalDateTime.of(2026, 6, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 6, 12, 0);

        testDataFactory.saveAvailability(user1, start, end.minusHours(1));
        testDataFactory.saveAvailability(user1, start.plusHours(1), end);

        testDataFactory.saveAvailability(user2, start, end);
        testDataFactory.saveAvailability(user2, start.minusHours(2), start);

        testDataFactory.saveAvailability(anotherUser, start, end);

        List<Availability> result = availabilityRepo.findAllByUserIdsAndDateRange(userIds, start, end);

        assertThat(result).hasSize(3);

        assertThat(result).extracting(a -> a.getUser().getId()).containsOnly(user1.getId(), user2.getId()).doesNotContain(anotherUser.getId());

        assertThat(result).extracting(Availability::getStartTime).isSorted();
    }

    @Test
    public void shouldReturnTrueWhenUserHaveRecordsInDateRange() {
        User user = testDataFactory.saveUser("user");

        User anotherUser = testDataFactory.saveUser("anotherUser");

        LocalDateTime start = LocalDateTime.of(2026, 6, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 6, 12, 0);

        testDataFactory.saveAvailability(user, start, end);

        assertThat(availabilityRepo.isFullyAvailable(
                user.getId(),
                start.minusHours(2),
                start.minusHours(1)))
                .isFalse();

        assertThat(availabilityRepo.isFullyAvailable(
                user.getId(),
                start,
                end))
                .isTrue();

        assertThat(availabilityRepo.isFullyAvailable(
                user.getId(),
                start.minusHours(1),
                start.plusHours(1)))
                .isFalse();

        assertThat(availabilityRepo.isFullyAvailable(
                anotherUser.getId(),
                start.minusHours(2),
                start.minusHours(1)))
                .isFalse();
    }
}

