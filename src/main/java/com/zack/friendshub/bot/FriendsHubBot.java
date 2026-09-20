package com.zack.friendshub.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class FriendsHubBot extends TelegramLongPollingBot {

    private final TelegramUpdateHandler telegramUpdateHandler;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage responseMessage = telegramUpdateHandler.handleUpdate(update);

        if (responseMessage != null) {
            try {
                execute(responseMessage);
            } catch (TelegramApiException e) {
                log.error("Error sending message to Telegram: {}", e.getMessage());
            }
        }
    }

    public void sendMessageToUser(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    public void sendMessageWithUserMenu(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .parseMode("HTML")
                .replyMarkup(TelegramKeyboardBuilder.buildUserMenu())
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}