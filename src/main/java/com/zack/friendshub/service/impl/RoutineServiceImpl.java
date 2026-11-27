package com.zack.friendshub.service.impl;

import com.zack.friendshub.dto.request.RoutineDto;
import com.zack.friendshub.model.UserRoutine;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.repository.UserRoutineRepo;
import com.zack.friendshub.service.RoutineService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineServiceImpl implements RoutineService {

    private final UserRoutineRepo userRoutineRepo;
    private final UserRepo userRepo;

    @Override
    public List<RoutineDto> getRoutines(Long userId) {
        List<UserRoutine> userRoutines = userRoutineRepo.findAllByUserId(userId);
        if (userRoutines == null || userRoutines.isEmpty()) {
            return List.of();
        }

        Map<String, List<UserRoutine>> grouped = userRoutines.stream()
                .collect(Collectors.groupingBy(r -> r.getName() + "|" + r.getStartTime() + "|" + r.getEndTime(), LinkedHashMap::new, Collectors.toList()));

        List<RoutineDto> result = new ArrayList<>();
        for (List<UserRoutine> group : grouped.values()) {
            UserRoutine first = group.get(0);
            List<Integer> days = group.stream()
                    .map(UserRoutine::getDayOfWeek)
                    .sorted()
                    .toList();
            result.add(new RoutineDto(first.getName(), days, first.getStartTime(), first.getEndTime()));
        }
        return result;
    }

    @Override
    @Transactional
    public void addRoutines(List<RoutineDto> routines, Long userId) {
        userRoutineRepo.deleteAllByUser_Id(userId);
        log.info("deleted old routines for User {}", userId);

        if (routines == null || routines.isEmpty()) {
            log.info("User {} saved empty (floating) schedule", userId);
            return;
        }

        List<UserRoutine> newRoutines = new ArrayList<>();

        for (RoutineDto dto : routines) {
            if (dto.daysOfWeek() == null || dto.daysOfWeek().isEmpty()) continue;

            for (Integer day : dto.daysOfWeek()) {
                UserRoutine routine = new UserRoutine();
                    routine.setUser(userRepo.getReferenceById(userId));
                    routine.setName(dto.name());
                    routine.setStartTime(dto.startTime());
                    routine.setEndTime(dto.endTime());
                    routine.setDayOfWeek(day);

                    newRoutines.add(routine);
            }
        }

        userRoutineRepo.saveAll(newRoutines);
        log.info("saved all routines for User {}", userId);
    }
}
