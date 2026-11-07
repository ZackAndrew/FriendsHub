package com.zack.friendshub.controller;

import com.zack.friendshub.dto.request.RoutineDto;
import com.zack.friendshub.security.UserPrincipal;
import com.zack.friendshub.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "Routines", description = "Operations related with routines")
public class RoutineController {

    private final RoutineService routineService;

    @Operation(
            summary = "Overwrite user routines",
            description = "Deletes all existing routines for the current user and saves the new ones. Send an empty list to clear the schedule."
    )
    @ApiResponse(responseCode = "200", description = "Routines successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PutMapping
    public ResponseEntity<Void> addRoutines(@Valid @RequestBody List<RoutineDto> routineDtos,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser.getId();
        log.info("Received request to update routines for User {}", userId);

        routineService.addRoutines(routineDtos, userId);
        return ResponseEntity.ok().build();
    }

}
