package com.zack.friendshub.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String token);

    void sendTelegramVerificationEmail(String toEmail, String token);
}
