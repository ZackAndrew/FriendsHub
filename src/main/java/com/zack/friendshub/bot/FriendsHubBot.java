package com.zack.friendshub.bot;

import com.zack.friendshub.enums.BotState;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.model.BotStateEntity;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;
import com.zack.friendshub.repository.BotStateRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.EmailService;
import com.zack.friendshub.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class FriendsHubBot extends TelegramLongPollingBot {

    private final UserRepo userRepo;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final BotStateRepo botStateRepo;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            BotState currentState = getCurrentState(chatId);
            Optional<User> userOpt = userRepo.findByTelegramChatId(chatId);

            if (currentState == BotState.WAITING_FOR_EMAIL) {
                handleLinkAccount(chatId, messageText.trim());
                setCurrentState(chatId, BotState.IDLE); // Скидаємо стан 🔄
                return;
            }

            if (messageText.equals("/start")) {
                if (userOpt.isPresent()) {
                    sendTextMessage(chatId, "Привіт, раді бачити тебе знову! Напиши /myfriends");
                } else {
                    sendMenuKeyboard(chatId, "Привіт! Вітаємо у FriendsHub! 🤝\nОбери дію на клавіатурі знизу:");
                }
            } else if (messageText.equals("🔗 Прив'язати акаунт")) {
                if (userOpt.isPresent()) {
                    sendTextMessage(chatId, "Твій Telegram-акаунт уже успішно прив'язаний до профілю! ✅");
                } else {
                    sendTextMessage(chatId, "Будь ласка, напиши свій email, який вказано на сайті:");
                    setCurrentState(chatId, BotState.WAITING_FOR_EMAIL); // Вмикаємо очікування пошти 🔄
                }
            } else if (messageText.equals("📝 Зареєструватися")) {
                if (userOpt.isPresent()) {
                    sendTextMessage(chatId, "Ти вже зареєстрований!");
                } else {
                    handleNewRegistration(chatId, update.getMessage().getFrom().getFirstName());
                }
            } else {
                sendMenuKeyboard(chatId, "Я не зрозумів цю команду. 🤷‍♂️ Будь ласка, скористайся кнопками меню.");
            }
        }
    }

    private void sendMenuKeyboard(long chatId, String text) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row = new KeyboardRow();
        row.add("🔗 Прив'язати акаунт");
        row.add("📝 Зареєструватися");

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void handleLinkAccount(Long chatId, String email) {
        Optional<User> existingUser = userRepo.findByEmail(email);


        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getTelegramChatId() != null) {
                sendTextMessage(chatId, "Цей email вже прив'язаний до іншого Telegram-акаунту! ❌");
                return;
            }

            VerificationToken token = verificationService.createTelegramVerificationToken(user, chatId);
            emailService.sendTelegramVerificationEmail(user.getEmail(), token.getToken());
            sendTextMessage(chatId, "На твою пошту " + email + " надіслано лист із підтвердженням! 📬\n" +
                    "Перевір скриньку та натисни кнопку в листі для завершення прив'язки.");

        } else {
            sendTextMessage(chatId, "Користувача з email " + email + " не знайдено на сайті. Перевірте правильність або напишіть /register.");
        }
    }

    public void sendTextMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void handleNewRegistration(Long chatId, String firstname) {
        User newUser = User.builder()
                .telegramChatId(chatId)
                .username("tg_" + chatId)
                .name(firstname)
                .role(Role.USER)
                .status(UserStatus.ACTIVATED)
                .dateOfRegistration(LocalDateTime.now())
                .build();
        userRepo.save(newUser);
        sendTextMessage(chatId, "Реєстрація успішна! 🥳 Твій ID для пошуку: " + newUser.getId());
    }

    public void setCurrentState(long chatId, BotState state) {
        BotStateEntity botStateEntity = botStateRepo.findById(chatId)
                .orElseGet(() -> BotStateEntity.builder()
                        .telegramChatId(chatId)
                        .build());

        botStateEntity.setCurrentState(state);

        botStateRepo.save(botStateEntity);
    }

    public BotState getCurrentState(long chatId) {
        return botStateRepo.findById(chatId)
                .map(BotStateEntity::getCurrentState)
                .orElse(BotState.IDLE);
    }
}