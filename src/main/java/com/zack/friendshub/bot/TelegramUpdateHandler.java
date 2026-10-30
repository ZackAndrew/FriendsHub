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

        if (messageText.equals("/start") || messageText.equalsIgnoreCase("Cancel")) {
            setCurrentState(chatId, BotState.IDLE);
            meetingDrafts.remove(chatId);
            if (messageText.equalsIgnoreCase("Cancel")) {
                SendMessage msg = new SendMessage(String.valueOf(chatId), "Action canceled. 🚫");
                msg.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                return List.of(msg);
            }
        }

        BotState currentState = getCurrentState(chatId);
        Optional<User> userOpt = userRepo.findByTelegramChatId(chatId);

        if (currentState == BotState.WAITING_FOR_EMAIL) {
            return List.of(handleLinkAccountEmailInput(chatId, messageText.trim()));
        }

        if (currentState == BotState.WAITING_FOR_MEETING_FRIEND_ID) {
            try {
                Long friendId = Long.parseLong(messageText.trim());

                Optional<User> friendOpt = userRepo.findById(friendId);
                if (friendOpt.isEmpty()) {
                    return List.of(new SendMessage(String.valueOf(chatId), "❌ User with this ID not found. Check your friends list and try again:"));
                }

                String friendUsername = friendOpt.get().getUsername();

                MeetingDraft draft = new MeetingDraft();
                draft.friendId = friendId;
                draft.participantUsername = friendUsername;
                meetingDrafts.put(chatId, draft);

                setCurrentState(chatId, BotState.WAITING_FOR_MEETING_TITLE);
                SendMessage msg = new SendMessage(String.valueOf(chatId), "✅ Selected friend: <b>" + friendOpt.get().getName() + "</b>.\n\nNow enter the <b>title</b> of the meeting (e.g., 'Coffee' or 'Project Sync'):");
                msg.setParseMode("HTML");
                return List.of(msg);
            } catch (NumberFormatException e) {
                return List.of(new SendMessage(String.valueOf(chatId), "❌ Error: ID must be a number. Try again:"));
            }
        }

        if (currentState == BotState.WAITING_FOR_MEETING_TITLE) {
            MeetingDraft draft = meetingDrafts.get(chatId);
            if (draft != null) {
                draft.title = messageText.trim();
            }
            setCurrentState(chatId, BotState.WAITING_FOR_MEETING_DATE);

            String exampleDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            SendMessage msg = new SendMessage(String.valueOf(chatId), "✅ Title saved.\n\nNow enter the <b>date and time</b> of the meeting in the format <code>YYYY-MM-DD HH:mm</code>\n(Example: " + exampleDate + "):");
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

                SendMessage msg = new SendMessage(String.valueOf(chatId), "🎉 <b>Meeting successfully created!</b> Request sent to your friend.");
                msg.setParseMode("HTML");
                msg.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                return List.of(msg);

            } catch (DateTimeParseException e) {
                SendMessage msg = new SendMessage(String.valueOf(chatId), "❌ Invalid date format. Please use the format <code>YYYY-MM-DD HH:mm</code>:");
                msg.setParseMode("HTML");
                return List.of(msg);
            } catch (Exception e) {
                setCurrentState(chatId, BotState.IDLE);
                meetingDrafts.remove(chatId);
                return List.of(new SendMessage(String.valueOf(chatId), "❌ Error creating meeting: " + e.getMessage()));
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
                    message.setText("Hello, <b>" + user.getName() + "</b>! Main menu opened 👇");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    message.setText("Hello, <b>" + firstName + "</b>! Welcome to FriendsHub! 🤝\nChoose an action from the keyboard below:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildGuestMenu());
                }
                messages.add(message);
                break;

            case "🔗 Link Account":
                if (isRegistered) {
                    message.setText("Your Telegram account is already successfully linked to your profile! ✅\nHere is your menu:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    message.setText("Please enter your email used on the website:");
                    setCurrentState(chatId, BotState.WAITING_FOR_EMAIL);
                }
                messages.add(message);
                break;

            case "📝 Register":
                if (isRegistered) {
                    message.setText("You are already registered! ✅\nHere is your menu:");
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                } else {
                    handleNewRegistration(chatId, firstName);
                    message.setText("Registration successful! 🥳 Your ID for searching: " + chatId);
                    message.setReplyMarkup(TelegramKeyboardBuilder.buildUserMenu());
                }
                messages.add(message);
                break;

            case "📅 My Meetings":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    messages.addAll(handleGetMyMeetings(chatId, user));
                }
                break;

            case "👥 My Friends":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText(handleGetMyFriends(user));
                    messages.add(message);
                }
                break;

            case "⚙️ Settings":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText("⚙️ <b>Profile Settings</b>\n\n" +
                            "👤 Name: " + user.getName() + "\n" +
                            "📧 Email: " + (user.getEmail() != null ? user.getEmail() : "Not specified") + "\n" +
                            "🔑 Role: " + user.getRole().name() + "\n\n" +
                            "<i>Your ID for friends:</i> <code>" + user.getId() + "</code>");
                    messages.add(message);
                }
                break;

            case "➕ New Meeting":
                if (!isRegistered) {
                    messages.add(showGuestWarning(chatId));
                } else {
                    message.setText("Let's schedule a meeting! 📅\n\nEnter the <b>friend's ID</b> you want to meet with (can be found in the 'My Friends' tab).\n\n<i>To cancel, type 'Cancel'</i>");
                    setCurrentState(chatId, BotState.WAITING_FOR_MEETING_FRIEND_ID);
                    messages.add(message);
                }
                break;

            default:
                message.setText("I didn't understand this command. 🤷‍♂️ Please use the menu buttons.");
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
                editMessageText.setText("✅ <b>Meeting accepted!</b>\n\n🔹 " + response.title() + "\n⏰ " + response.startTime().toString());

            } else if (callbackData.startsWith("DECLINE_")) {
                Long meetingId = Long.parseLong(callbackData.split("_")[1]);
                MeetingResponseDto response = meetingService.declineMeetingRequest(meetingId, userPrincipal);
                editMessageText.setText("❌ <b>Meeting declined.</b>\n\n🔹 " + response.title());

            } else if (callbackData.startsWith("CANCEL_")) {
                Long meetingId = Long.parseLong(callbackData.split("_")[1]);
                MeetingResponseDto response = meetingService.cancelMeetingRequest(meetingId, userPrincipal);
                editMessageText.setText("🗑 <b>Meeting request canceled.</b>\n\n🔹 " + response.title());

            } else {
                editMessageText.setText("Unknown action.");
            }
        } catch (Exception e) {
            log.error("Error processing callback query: ", e);
            editMessageText.setText("An error occurred while processing the request. 😔 (" + e.getMessage() + ")");
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
                message.setText("This email is already linked to another Telegram account! ❌");
            } else {
                VerificationToken token = verificationService.createTelegramVerificationToken(user, chatId);
                emailService.sendTelegramVerificationEmail(user.getEmail(), token.getToken());
                message.setText("A confirmation email has been sent to <b>" + email + "</b>! 📬\n" +
                        "Check your inbox and click the button in the email to complete the linking.");
            }
        } else {
            message.setText("User with email " + email + " not found on the website. Check the spelling or register via /register.");
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
        msg.setText("You need to register or log in first! 🔒");
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
            emptyMsg.setText("You have no scheduled meetings yet. 📭");
            messages.add(emptyMsg);
            return messages;
        }

        SendMessage header = new SendMessage();
        header.setChatId(String.valueOf(chatId));
        header.setText("<b>Your Meetings:</b>");
        header.setParseMode("HTML");
        messages.add(header);

        for (MeetingResponseDto m : meetings) {
            String otherPerson = m.organizerId().equals(user.getId()) ? m.participantName() : m.organizerName();

            String text = "🔹 <b>" + m.title() + "</b>\n" +
                    "👤 With: " + otherPerson + "\n" +
                    "⏰ Start: " + m.startTime().toString() + "\n" +
                    "📌 Status: " + m.status().name();

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
            return "You don't have any friends in FriendsHub yet. 😔\n\n" +
                    "Share your ID with friends: <code>" + user.getId() + "</code>";
        }

        StringBuilder sb = new StringBuilder("👥 <b>Your Friends:</b>\n\n");
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