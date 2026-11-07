package com.zack.friendshub.service;

import com.zack.friendshub.dto.request.RoutineDto;

import java.util.List;

public interface RoutineService {
    void addRoutines(List<RoutineDto> routines, Long userId);
}
