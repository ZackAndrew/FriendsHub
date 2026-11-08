package com.zack.friendshub.controller;

import com.zack.friendshub.service.AvailabilityService;
import com.zack.friendshub.service.GoogleCalendarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Operations related with availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final GoogleCalendarService googleCalendarService;




}
