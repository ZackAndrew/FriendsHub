package com.zack.friendshub.service.impl;

import com.zack.friendshub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Override
    public void sendPasswordResetTokenEmail(String toEmail, String token) {
        String resetLink = "http://localhost:" + serverPort + "/reset-password?token" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - FriendsHub");

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                        <h2 style="color: #4A90E2;">Password Reset Request</h2>
                        <p>We received a request to reset the password for your <b>FriendsHub</b> account.</p>
                        <p>To set a new password, please click the button below (the link is valid for 15 minutes):</p>
                        <a href="%s" style="display: inline-block; padding: 12px 24px; margin-top: 10px; background-color: #4A90E2; color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: bold;">Reset Password</a>
                        <p style="margin-top: 20px; font-size: 12px; color: #777;">
                            If the button doesn't work, copy and paste this link into your browser:<br>
                            <a href="%s">%s</a>
                        </p>
                        <p style="margin-top: 30px;">If you didn't request a password reset, you can safely ignore and delete this email. Your password will remain unchanged.</p>
                    </div>
                    """.formatted(resetLink, resetLink, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email successfully sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error sending password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
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
