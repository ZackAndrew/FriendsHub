package com.zack.friendshub.model;

import com.zack.friendshub.enums.BotState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bot_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotStateEntity {

    @Id
    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private BotState currentState;
}