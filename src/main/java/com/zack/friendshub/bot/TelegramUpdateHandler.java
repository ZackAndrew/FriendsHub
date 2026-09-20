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
import com.zack.friendshub.service.MeetingService;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final UserRepo userRepo;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final BotStateRepo botStateRepo;
    private final MeetingService meetingService;

    public SendMessage handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();

        BotState currentState = getCurrentState(chatId);
        Optional<User> userOpt = userRepo.findByTelegramChatId(chatId);

        if (currentState == BotState.WAITING_FOR_EMAIL) {
            return handleLinkAccountEmailInput(chatId, messageText.trim());
        }

        return processCommand(chatId, messageText, firstName, userOpt.orElse(null));
    }

    private SendMessage processCommand(long chatId, String messageText, String firstName, User user) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setParseMode("HTML");

        boolean isRegistered = (user != null);

        switch (messageText) {
            case "/start":
                if (isRegistered) {
                    message.setText("Привіт, <b>" + user.getName() + "</b>! Головне меню відкрито 👇");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    message.setText("Привіт, <b>" + firstName + "</b>! Вітаємо у FriendsHub! 🤝\nОбери дію на клавіатурі знизу:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildGuestMenu());
                }
                break;

            case "🔗 Прив'язати акаунт":
                if (isRegistered) {
                    message.setText("Твій Telegram-акаунт уже успішно прив'язаний до профілю! ✅\nОсь твоє меню:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu()); // ДОДАТИ ЦЕ
                } else {
                    message.setText("Будь ласка, напиши свій email, який вказано на сайті:");
                    setCurrentState(chatId, BotState.WAITING_FOR_EMAIL);
                }
                break;

            case "📝 Зареєструватися":
                if (isRegistered) {
                    message.setText("Ти вже зареєстрований! ✅\nОсь твоє меню:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu()); // ДОДАТИ ЦЕ
                } else {
                    handleNewRegistration(chatId, firstName);
                    message.setText("Реєстрація успішна! 🥳 Твій ID для пошуку: " + chatId);
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                }
                break;

            case "📅 Мої зустрічі":
                if (!isRegistered) return showGuestWarning(chatId);
                message.setText(handleGetMyMeetings(user));
                break;

            case "👥 Мої друзі":
                if (!isRegistered) return showGuestWarning(chatId);
                message.setText("Функція перегляду друзів у розробці 🛠️");
                break;

            default:
                message.setText("Я не зрозумів цю команду. 🤷‍♂️ Будь ласка, скористайся кнопками меню.");
                message.setReplyMarkup(isRegistered ? TelegramKeyboardBuilder.buildUserMenu() : TelegramKeyboardBuilder.buildGuestMenu());
                break;
        }
        return message;
    }

    private SendMessage handleLinkAccountEmailInput(Long chatId, String email) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        setCurrentState(chatId, BotState.IDLE);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getTelegramChatId() != null) {
                message.setText("Цей email вже прив'язаний до іншого Telegram-акаунту! ❌");
            } else {
                VerificationToken token = verificationService.createTelegramVerificationToken(user, chatId);
                emailService.sendTelegramVerificationEmail(user.getEmail(), token.getToken());
                message.setText("На твою пошту <b>" + email + "</b> надіслано лист із підтвердженням! 📬\n" +
                        "Перевір скриньку та натисни кнопку в листі для завершення прив'язки.");
            }
        } else {
            message.setText("Користувача з email " + email + " не знайдено на сайті. Перевірте правильність або напишіть /register.");
        }
        return message;
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
    }

    private SendMessage showGuestWarning(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Спочатку потрібно зареєструватися або увійти! 🔒");
        msg.setReplyMarkup(TelegramKeyboardBuilder.buildGuestMenu());
        return msg;
    }

    private String handleGetMyMeetings(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        List<MeetingResponseDto> meetings = meetingService.getUserMeetings(userPrincipal);

        if (meetings.isEmpty()) {
            return "У вас поки немає запланованих зустрічей. 📭";
        }

        StringBuilder sb = new StringBuilder("<b>Ваші зустрічі:</b>\n\n");
        for (MeetingResponseDto m : meetings) {

            String otherPerson = m.organizerId().equals(user.getId()) ? m.participantName() : m.organizerName();

            sb.append("🔹 <b>").append(m.title()).append("</b>\n");
            sb.append("👤 З ким: ").append(otherPerson).append("\n");

            sb.append("⏰ Початок: ").append(m.startTime().toString()).append("\n");
            sb.append("📌 Статус: ").append(m.status().name()).append("\n\n");
        }
        return sb.toString();
    }

    private void setCurrentState(long chatId, BotState state) {
        BotStateEntity botStateEntity = botStateRepo.findById(chatId)
                .orElseGet(() -> BotStateEntity.builder().telegramChatId(chatId).build());
        botStateEntity.setCurrentState(state);
        botStateRepo.save(botStateEntity);
    }

    private BotState getCurrentState(long chatId) {
        return botStateRepo.findById(chatId)
                .map(BotStateEntity::getCurrentState)
                .orElse(BotState.IDLE);
    }
}