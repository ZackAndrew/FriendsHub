package com.zack.friendshub.bot.config;

import com.zack.friendshub.bot.FriendsHubBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "telegram.bot.token", matchIfMissing = false)
public class TelegramBotConfig {

    @Value("${telegram.bot.username:}")
    private String botUsername;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Bean
    public FriendsHubBot friendsHubBot() {
        return new FriendsHubBot(botUsername, botToken);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(FriendsHubBot friendsHubBot) throws TelegramApiException {
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram bot token is not set. Bot will not be registered.");
            return null;
        }
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(friendsHubBot);
        log.info("Telegram bot registered successfully");
        return botsApi;
    }
}
