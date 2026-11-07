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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineServiceImpl implements RoutineService {

    private final UserRoutineRepo userRoutineRepo;
    private final UserRepo userRepo;

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
