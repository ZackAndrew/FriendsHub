package com.zack.friendshub.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.zack.friendshub.model.Meeting;
import com.zack.friendshub.model.UserIntegration;
import com.zack.friendshub.repository.UserIntegrationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.api.client.json.gson.GsonFactory.getDefaultInstance;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final UserIntegrationRepo userIntegrationRepo;

    @Value("${OAUTH_CLIENT_ID:dummy-google-client-id}")
    private String clientId;
    @Value("${OAUTH_CLIENT_SECRET:dummy-google-client-secret}")
    private String clientSecret;

    public Credential getCredentialForUser(Long userId) {
        UserIntegration userIntegration =
                userIntegrationRepo.findByUserIdAndProvider(userId, "GOOGLE")
                        .orElseThrow(() -> new RuntimeException("Google integration not found"));

        return new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(userIntegration.getRefreshToken());
    }

    public List<Event> getUserEvents(Long userId, LocalDateTime windowStart, LocalDateTime windowEnd) {
        try {
            Credential credential = getCredentialForUser(userId);

            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    getDefaultInstance(),
                    credential)
                    .setApplicationName("FriendHub")
                    .build();

            ZoneId zone = ZoneId.systemDefault();
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

    public String createMeetingEvent(Long organizerId, Meeting meeting, List<String> attendeeEmails) {
        boolean hasGoogle = userIntegrationRepo.findByUserIdAndProvider(organizerId, "GOOGLE").isPresent();
        if (!hasGoogle) {
            log.info("User ID {} hasn't linked Google Calendar. Skipping event creation.", organizerId);
            return null;
        }

        Event event = new Event();
        event.setSummary(meeting.getTitle());

        ZoneId zoneId = ZoneId.systemDefault();
        EventDateTime startDateTime = convertLocalDateTimeToEventDateTime(meeting.getStartTime(), zoneId);
        EventDateTime endDateTime = convertLocalDateTimeToEventDateTime(meeting.getEndTime(), zoneId);

        event.setStart(startDateTime);
        event.setEnd(endDateTime);

        if (attendeeEmails != null && !attendeeEmails.isEmpty()) {
            List<EventAttendee> eventAttendees = new ArrayList<>();
            for (String email : attendeeEmails) {
                EventAttendee eventAttendee = new EventAttendee();
                eventAttendee.setEmail(email);
                eventAttendees.add(eventAttendee);
            }
            event.setAttendees(eventAttendees);
        }


        try {
            Credential credential = getCredentialForUser(organizerId);

            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    getDefaultInstance(),
                    credential)
                    .setApplicationName("FriendHub")
                    .build();

            Event createdEvent = service.events().insert("primary", event).
                    setSendUpdates("all")
                    .execute();

            return createdEvent.getId();
        } catch (Exception e) {
            log.error("Failed to create Google Calendar event for organizerId {}: {}", organizerId, e.getMessage());
            return null;
        }
    }

    public void deleteEvent(Long organizerId, String eventId) {
        boolean hasGoogle = userIntegrationRepo.findByUserIdAndProvider(organizerId, "GOOGLE").isPresent();
        if (!hasGoogle || eventId == null) {
            return;
        }

        try {
            Credential credential = getCredentialForUser(organizerId);
            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    getDefaultInstance(),
                    credential)
                    .setApplicationName("FriendHub")
                    .build();

            service.events().delete("primary", eventId)
                    .setSendUpdates("all")
                    .execute();
        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event: {}", e.getMessage());
        }
    }

    private EventDateTime convertLocalDateTimeToEventDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        DateTime dateTime = new DateTime(zonedDateTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return new EventDateTime()
                .setDateTime(dateTime)
                .setTimeZone(zoneId.getId());
    }
}
