package com.zack.friendshub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 30)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDateTime dateOfRegistration;

    @Column(nullable = false)
    @JsonIgnore
    private String passwordHash;
}
