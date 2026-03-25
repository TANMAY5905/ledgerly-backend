package com.expensetracker.expense_tracker.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendResetEmail(String to, String resetLink) {
        logger.info("Starting email delivery for: {}", to);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String htmlMsg = "<h3>Password Reset Request</h3>"
                    + "<p>You requested to reset your password. Click the link below to proceed:</p>"
                    + "<a href=\"" + resetLink + "\">Reset My Password</a>"
                    + "<br><br>"
                    + "<p>This link will expire in 15 minutes.</p>"
                    + "<p>If you didn't request this, please ignore this email.</p>";

            helper.setText(htmlMsg, true);
            helper.setTo(to);
            helper.setSubject("Reset your Ledgerly Password");
            helper.setFrom("ledgerly.info@gmail.com"); // Set to your authenticated email
            
            mailSender.send(mimeMessage);
            logger.info("Successfully sent reset email to: {}", to);
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to send reset email to {}. Error: {}", to, e.getMessage());
            // In a real app, you might want to save this failure to the DB
        }
    }
}
