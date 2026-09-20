package com.zack.friendshub.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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
}