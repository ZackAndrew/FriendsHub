package com.zack.friendshub.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class FriendsHubBot extends TelegramLongPollingBot {

    private final String botUsername;

    public FriendsHubBot(String botUsername, String botToken) {
        super(botToken);
        this.botUsername = botUsername;
        log.info("Telegram bot initialized: username={}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.info("Received message from chat {}: {}", chatId, messageText);

            String responseText = switch (messageText) {
                case "/start" -> "Welcome to FriendsHub! 🎉\nUse /help to see available commands.";
                case "/help" -> "Available commands:\n/start - Welcome\n/help - Show this message";
                default -> "I don't understand that command. Try /help";
            };

            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(responseText);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
            }
        }
    }
}
