package com.zack.friendshub.model;

import com.zack.friendshub.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(value = EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime createdAt;
}
