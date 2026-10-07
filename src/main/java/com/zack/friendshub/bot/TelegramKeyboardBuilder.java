package com.zack.friendshub.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class TelegramKeyboardBuilder {

    public static ReplyKeyboardMarkup buildGuestMenu() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("🔗 Прив'язати акаунт");
        row.add("📝 Зареєструватися");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup buildUserMenu() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📅 Мої зустрічі");
        row1.add("👥 Мої друзі");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("➕ Нова зустріч");
        row2.add("⚙️ Налаштування");

        keyboardMarkup.setKeyboard(List.of(row1, row2));
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup buildMeetingActionsMenu(Long meetingId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton acceptBtn = new InlineKeyboardButton();
        acceptBtn.setText("✅ Прийняти");
        acceptBtn.setCallbackData("ACCEPT_" + meetingId);

        InlineKeyboardButton declineBtn = new InlineKeyboardButton();
        declineBtn.setText("❌ Відхилити");
        declineBtn.setCallbackData("DECLINE_" + meetingId);

        rowInline.add(acceptBtn);
        rowInline.add(declineBtn);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public static InlineKeyboardMarkup buildCancelMeetingMenu(Long meetingId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
        cancelBtn.setText("🗑 Скасувати запит");
        cancelBtn.setCallbackData("CANCEL_" + meetingId);

        rowInline.add(cancelBtn);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}