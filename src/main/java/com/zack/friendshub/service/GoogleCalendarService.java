package com.zack.friendshub.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import com.zack.friendshub.model.UserIntegration;
import com.zack.friendshub.repository.UserIntegrationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final UserIntegrationRepo userIntegrationRepo;

    @Value("${OAUTH_CLIENT_ID}")
    private String clientId;
    @Value("${OAUTH_CLIENT_SECRET}")
    private String clientSecret;

    public Credential getCredentialForUser(Long userId) {
        UserIntegration userIntegration =
                userIntegrationRepo.findByUserIdAndProvider(userId, "GOOGLE")
                        .orElseThrow(() -> new RuntimeException("Google integration not found"));

        return new GoogleCredential.Builder()
                .setTransport(new com.google.api.client.http.javanet.NetHttpTransport())
                .setJsonFactory(com.google.api.client.json.gson.GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(userIntegration.getRefreshToken());
    }

    public List<Event> getUserEvents(Long userId, java.time.LocalDateTime windowStart, java.time.LocalDateTime windowEnd) {
        try {
            Credential credential = getCredentialForUser(userId);

            Calendar service = new Calendar.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("FriendHub")
                    .build();

            java.time.ZoneId zone = java.time.ZoneId.systemDefault();
            DateTime timeMin = new DateTime(windowStart.atZone(zone).toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(windowEnd.atZone(zone).toInstant().toEpochMilli());

            List<String> activeCalendarIds = getActiveEventIds(service);
            List<Event> allEvents = new ArrayList<>();

            for (String calendarId : activeCalendarIds) {
                try {
                    Events events = service.events().list(calendarId)
                            .setTimeMin(timeMin)
                            .setTimeMax(timeMax)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();

                    if (events.getItems() != null) {
                        allEvents.addAll(events.getItems());
                    }
                } catch (Exception e) {
                    log.warn("Failed to get events for calendar {}: {}", calendarId, e.getMessage());
                }
            }

            allEvents.sort(Comparator.comparingLong(this::getEventStartTime));
            return allEvents;

        } catch (Exception e) {
            log.error("Failed to fetch Google Calendar events for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private long getEventStartTime(Event event) {
        return event.getStart().getDateTime() != null ?
                event.getStart().getDateTime().getValue() : event.getStart().getDate().getValue();
    }

    private List<String> getActiveEventIds(Calendar service) throws Exception {
        CalendarList calendarList = service.calendarList().list().execute();

        return calendarList.getItems().stream()
                .map(CalendarListEntry::getId)
                .filter(id -> !id.contains("holiday@group.v.calendar.google.com"))
                .toList();
    }
}
