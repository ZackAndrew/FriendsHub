package com.zack.friendshub.bot;

import com.zack.friendshub.dto.response.friendship.FriendDto;
import com.zack.friendshub.enums.BotState;
import com.zack.friendshub.enums.Role;
import com.zack.friendshub.enums.UserStatus;
import com.zack.friendshub.enums.MeetingStatus;
import com.zack.friendshub.model.BotStateEntity;
import com.zack.friendshub.model.User;
import com.zack.friendshub.model.VerificationToken;
import com.zack.friendshub.repository.BotStateRepo;
import com.zack.friendshub.repository.UserRepo;
import com.zack.friendshub.service.EmailService;
import com.zack.friendshub.service.VerificationService;
import com.zack.friendshub.service.MeetingService;
import com.zack.friendshub.service.FriendshipService;
import com.zack.friendshub.dto.response.meeting.MeetingResponseDto;
import com.zack.friendshub.dto.request.MeetingRequestDto;
import com.zack.friendshub.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final UserRepo userRepo;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final BotStateRepo botStateRepo;
    private final MeetingService meetingService;
    private final FriendshipService friendshipService;

    // Сховище для чернеток зустрічей (Машина станів)
    private final Map<Long, MeetingDraft> meetingDrafts = new ConcurrentHashMap<>();

    private static class MeetingDraft {
        String participantUsername;
        Long friendId;
        String title;
    }

    public List<SendMessage> handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();

        // Аварійний вихід зі станів (якщо користувач передумав створювати зустріч)
        if (messageText.equals("/start") || messageText.equalsIgnoreCase("Скасувати")) {
            setCurrentState(chatId, BotState.IDLE);
            meetingDrafts.remove(chatId);
            if (messageText.equalsIgnoreCase("Скасувати")) {
                SendMessage msg = new SendMessage(String.valueOf(chatId), "Дію скасовано. 🚫");
                msg.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                return List.of(msg);
            }
        }

        BotState currentState = getCurrentState(chatId);
        Optional<User> userOpt = userRepo.findByTelegramChatId(chatId);

        // --- ОБРОБКА СТАНІВ ---
        if (currentState == BotState.WAITING_FOR_EMAIL) {
            return List.of(handleLinkAccountEmailInput(chatId, messageText.trim()));
        }

        if (currentState == BotState.WAITING_FOR_MEETING_FRIEND_ID) {
            try {
                Long friendId = Long.parseLong(messageText.trim());

                // ШУКАЄМО ДРУГА В БАЗІ ЗА ID
                Optional<User> friendOpt = userRepo.findById(friendId);
                if (friendOpt.isEmpty()) {
                    return List.of(new SendMessage(String.valueOf(chatId), "❌ Користувача з таким ID не знайдено. Перевірте список друзів та спробуйте ще раз:"));
                }

                String friendUsername = friendOpt.get().getUsername();

                MeetingDraft draft = new MeetingDraft();
                draft.friendId = friendId;
                draft.participantUsername = friendUsername; // ЗБЕРІГАЄМО ЮЗЕРНЕЙМ ДЛЯ DTO
                meetingDrafts.put(chatId, draft);

                setCurrentState(chatId, BotState.WAITING_FOR_MEETING_TITLE);
                SendMessage msg = new SendMessage(String.valueOf(chatId), "✅ Обрано друга: <b>" + friendOpt.get().getName() + "</b>.\n\nТепер введіть <b>назву</b> зустрічі (наприклад, 'Кава' або 'Обговорення проєкту'):");
                msg.setParseMode("HTML");
                return List.of(msg);
            } catch (NumberFormatException e) {
                return List.of(new SendMessage(String.valueOf(chatId), "❌ Помилка: ID має бути числом. Спробуйте ще раз:"));
            }
        }

        if (currentState == BotState.WAITING_FOR_MEETING_TITLE) {
            MeetingDraft draft = meetingDrafts.get(chatId);
            if (draft != null) {
                draft.title = messageText.trim();
            }
            setCurrentState(chatId, BotState.WAITING_FOR_MEETING_DATE);

            String exampleDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            SendMessage msg = new SendMessage(String.valueOf(chatId), "✅ Назву збережено.\n\nТепер введіть <b>дату та час</b> зустрічі у форматі <code>РРРР-ММ-ДД ГГ:ХХ</code>\n(Наприклад: " + exampleDate + "):");
            msg.setParseMode("HTML");
            return List.of(msg);
        }

        if (currentState == BotState.WAITING_FOR_MEETING_DATE) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime startTime = LocalDateTime.parse(messageText.trim(), formatter);
                MeetingDraft draft = meetingDrafts.get(chatId);

                MeetingRequestDto requestDto = MeetingRequestDto.builder()
                        .participantUsername(draft.participantUsername)
                        .startTime(startTime)
                        .endTime(startTime.plusHours(1))
                        .build();

                meetingService.sendMeetingRequest(requestDto, new UserPrincipal(userOpt.get()));

                meetingDrafts.remove(chatId);
                setCurrentState(chatId, BotState.IDLE);

                SendMessage msg = new SendMessage(String.valueOf(chatId), "🎉 <b>Зустріч успішно створена!</b> Запит надіслано вашому другу.");
                msg.setParseMode("HTML");
                msg.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                return List.of(msg);

            } catch (DateTimeParseException e) {
                SendMessage msg = new SendMessage(String.valueOf(chatId), "❌ Неправильний формат дати. Будь ласка, використовуйте формат <code>РРРР-ММ-ДД ГГ:ХХ</code>:");
                msg.setParseMode("HTML");
                return List.of(msg);
            } catch (Exception e) {
                setCurrentState(chatId, BotState.IDLE);
                meetingDrafts.remove(chatId);
                return List.of(new SendMessage(String.valueOf(chatId), "❌ Помилка при створенні зустрічі: " + e.getMessage()));
            }
        }

        return processCommand(chatId, messageText, firstName, userOpt.orElse(null));
    }

    private List<SendMessage> processCommand(long chatId, String messageText, String firstName, User user) {
        List<SendMessage> messages = new ArrayList<>();
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
                messages.add(message);
                break;

            case "🔗 Прив'язати акаунт":
                if (isRegistered) {
                    message.setText("Твій Telegram-акаунт уже успішно прив'язаний до профілю! ✅\nОсь твоє меню:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    message.setText("Будь ласка, напиши свій email, який вказано на сайті:");
                    setCurrentState(chatId, BotState.WAITING_FOR_EMAIL);
                }
                messages.add(message);
                break;

            case "📝 Зареєструватися":
                if (isRegistered) {
                    message.setText("Ти вже зареєстрований! ✅\nОсь твоє меню:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    handleNewRegistration(chatId, firstName);
                    message.setText("Реєстрація успішна! 🥳 Твій ID для пошуку: " + chatId);
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                }
                messages.add(message);
                break;

            case "📅 Мої зустрічі":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    messages.addAll(handleGetMyMeetings(chatId, user));
                }
                break;

            case "👥 Мої друзі":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText(handleGetMyFriends(user));
                    messages.add(message);
                }
                break;

            case "⚙️ Налаштування":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText("⚙️ <b>Налаштування профілю</b>\n\n" +
                            "👤 Ім'я: " + user.getName() + "\n" +
                            "📧 Email: " + (user.getEmail() != null ? user.getEmail() : "Не вказано") + "\n" +
                            "🔑 Роль: " + user.getRole().name() + "\n\n" +
                            "<i>Ваш ID для друзів:</i> <code>" + user.getId() + "</code>");
                    messages.add(message);
                }
                break;

            case "➕ Нова зустріч":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText("Давайте заплануємо зустріч! 📅\n\nВведіть <b>ID друга</b>, з яким хочете зустрітися (його можна подивитися у вкладці 'Мої друзі').\n\n<i>Щоб скасувати, напишіть 'Скасувати'</i>");
                    setCurrentState(chatId, BotState.WAITING_FOR_MEETING_FRIEND_ID);
                    messages.add(message);
                }
                break;

            default:
                message.setText("Я не зрозумів цю команду. 🤷‍♂️ Будь ласка, скористайся кнопками меню.");
                message.setReplyMarkup(isRegistered ? TelegramKeyboardBuilder.buildUserMenu() : TelegramKeyboardBuilder.buildGuestMenu());
                messages.add(message);
                break;
        }
        return messages;
    }

    public EditMessageText handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        Optional<User> userOpt = userRepo.findByTelegramChatId(chatId);
        if (userOpt.isEmpty()) {
            return null;
        }
        UserPrincipal userPrincipal = new UserPrincipal(userOpt.get());

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setParseMode("HTML");

        try {
            if (callbackData.startsWith("ACCEPT_")) {
                Long meetingId = Long.parseLong(callbackData.split("_")[1]);
                MeetingResponseDto response = meetingService.acceptMeetingRequest(meetingId, userPrincipal);
                editMessageText.setText("✅ <b>Зустріч прийнято!</b>\n\n🔹 " + response.title() + "\n⏰ " + response.startTime().toString());

            } else if (callbackData.startsWith("DECLINE_")) {
                Long meetingId = Long.parseLong(callbackData.split("_")[1]);
                MeetingResponseDto response = meetingService.declineMeetingRequest(meetingId, userPrincipal);
                editMessageText.setText("❌ <b>Зустріч відхилено.</b>\n\n🔹 " + response.title());

            } else if (callbackData.startsWith("CANCEL_")) {
                Long meetingId = Long.parseLong(callbackData.split("_")[1]);
                MeetingResponseDto response = meetingService.cancelMeetingRequest(meetingId, userPrincipal);
                editMessageText.setText("🗑 <b>Запит на зустріч скасовано.</b>\n\n🔹 " + response.title());

            } else {
                editMessageText.setText("Невідома дія.");
            }
        } catch (Exception e) {
            log.error("Error processing callback query: ", e);
            editMessageText.setText("Виникла помилка під час обробки запиту. 😔 (" + e.getMessage() + ")");
        }

        return editMessageText;
    }

    private SendMessage handleLinkAccountEmailInput(Long chatId, String email) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setParseMode("HTML");

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

    private List<SendMessage> handleGetMyMeetings(long chatId, User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        List<MeetingResponseDto> meetings = meetingService.getUserMeetings(userPrincipal);
        List<SendMessage> messages = new ArrayList<>();

        if (meetings.isEmpty()) {
            SendMessage emptyMsg = new SendMessage();
            emptyMsg.setChatId(String.valueOf(chatId));
            emptyMsg.setText("У вас поки немає запланованих зустрічей. 📭");
            messages.add(emptyMsg);
            return messages;
        }

        SendMessage header = new SendMessage();
        header.setChatId(String.valueOf(chatId));
        header.setText("<b>Ваші зустрічі:</b>");
        header.setParseMode("HTML");
        messages.add(header);

        for (MeetingResponseDto m : meetings) {
            String otherPerson = m.organizerId().equals(user.getId()) ? m.participantName() : m.organizerName();

            String text = "🔹 <b>" + m.title() + "</b>\n" +
                    "👤 З ким: " + otherPerson + "\n" +
                    "⏰ Початок: " + m.startTime().toString() + "\n" +
                    "📌 Статус: " + m.status().name();

            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText(text);
            msg.setParseMode("HTML");

            if (m.status() == MeetingStatus.PENDING) {
                if (!m.organizerId().equals(user.getId())) {
                    msg.setReplyMarkup(TelegramKeyboardBuilder.buildMeetingActionsMenu(m.id()));
                } else {
                    msg.setReplyMarkup(TelegramKeyboardBuilder.buildCancelMeetingMenu(m.id()));
                }
            }
            messages.add(msg);
        }
        return messages;
    }

    private String handleGetMyFriends(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        List<FriendDto> friends = friendshipService.getAllFriends(userPrincipal);

        if (friends == null || friends.isEmpty()) {
            return "У вас поки що немає друзів у FriendsHub. 😔\n\n" +
                    "Поділіться своїм ID з друзями: <code>" + user.getId() + "</code>";
        }

        StringBuilder sb = new StringBuilder("👥 <b>Ваші друзі:</b>\n\n");
        for (FriendDto friend : friends) {
            sb.append("👤 <b>").append(friend.name()).append("</b>");
            if (friend.username() != null) {
                sb.append(" (@").append(friend.username()).append(")");
            }
            sb.append(" — ID: <code>").append(friend.userId()).append("</code>\n");
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