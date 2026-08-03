package com.zack.friendshub.service.impl;

import com.zack.friendshub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Підтвердження реєстрації у FriendsHub");

            String verificationUrl = "http://localhost:" + serverPort + "/api/auth/verify/" + token;

            String htmlContent = buildEmailTemplate(verificationUrl);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendTelegramVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Прив'язка до FriendsHub Telegram bot");
            String verificationUrl = "http://localhost:" + serverPort + "/api/auth/verify-telegram/" + token;

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px; text-align: center;\">" +
                    "  <div style=\"max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                    "    <h2 style=\"color: #333333;\">Запит на прив'язку Telegram 🤝</h2>" +
                    "    <p style=\"color: #666666; font-size: 16px;\">Хтось (сподіваємось, ви) намагається прив'язати цей email до Telegram-бота FriendsHub.</p>" +
                    "    <p style=\"color: #666666; font-size: 16px;\">Щоб підтвердити, що це ваш акаунт, натисніть кнопку нижче:</p>" +
                    "    <a href=\"" + verificationUrl + "\" style=\"display: inline-block; background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; margin-top: 20px;\">" +
                    "      Підтвердити прив'язку" +
                    "    </a>" +
                    "    <p style=\"color: #999999; font-size: 12px; margin-top: 30px;\">Якщо ви не надсилали запит у боті, просто проігноруйте цей лист.</p>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);


        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


    private String buildEmailTemplate(String url) {
        return "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px; text-align: center;\">" +
                "  <div style=\"max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                "    <h2 style=\"color: #333333;\">Ласкаво просимо до FriendsHub! 🎉</h2>" +
                "    <p style=\"color: #666666; font-size: 16px;\">Дякуємо за реєстрацію. Будь ласка, підтвердьте свій акаунт, натиснувши на кнопку нижче:</p>" +
                "    <a href=\"" + url + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; margin-top: 20px;\">" +
                "      Активувати акаунт" +
                "    </a>" +
                "    <p style=\"color: #999999; font-size: 12px; margin-top: 30px;\">Якщо ви не реєструвалися на нашому сайті, просто ігноруйте цей лист.</p>" +
                "  </div>" +
                "</div>";
    }
}
