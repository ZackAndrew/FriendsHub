package com.zack.friendshub.model;

import com.zack.friendshub.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meeting")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "ent_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;

    @Column(name = "title", nullable = false)
    private String title;
}
