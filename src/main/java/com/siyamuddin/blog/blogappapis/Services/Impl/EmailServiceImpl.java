package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Config.Properties.EmailProperties;
import com.siyamuddin.blog.blogappapis.Services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailProperties emailProperties;
    
    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        if (!emailProperties.getEnabled() || mailSender == null) {
            log.warn("Email sending is disabled or mail sender not configured. Would send to: {}, subject: {}", to, subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to SAAS Starter!";
        String body = String.format(
            "Hello %s,\n\n" +
            "Welcome to SAAS Starter! We're excited to have you on board.\n\n" +
            "Your account has been successfully created. You can now start using our platform.\n\n" +
            "Best regards,\n" +
            "SAAS Starter Team",
            name
        );
        sendEmail(to, subject, body);
    }
    
    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String verificationToken) {
        String subject = "Verify Your Email Address";
        String verificationUrl = emailProperties.getVerificationBaseUrl() + "?token=" + verificationToken;
        String body = String.format(
            "Hello %s,\n\n" +
            "Please verify your email address by clicking on the following link:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create an account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "SAAS Starter Team",
            name, verificationUrl
        );
        sendEmail(to, subject, body);
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        String subject = "Reset Your Password";
        String resetUrl = emailProperties.getPasswordResetBaseUrl() + "?token=" + resetToken;
        String body = String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Click on the following link to reset it:\n\n" +
            "%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "SAAS Starter Team",
            name, resetUrl
        );
        sendEmail(to, subject, body);
    }
    
    @Override
    @Async
    public void sendAccountLockedEmail(String to, String name, int lockoutDurationMinutes) {
        String subject = "Account Temporarily Locked";
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been temporarily locked due to multiple failed login attempts.\n\n" +
            "Your account will be unlocked in %d minutes. Please try again after that time.\n\n" +
            "If you did not attempt to log in, please contact support immediately.\n\n" +
            "Best regards,\n" +
            "SAAS Starter Team",
            name, lockoutDurationMinutes
        );
        sendEmail(to, subject, body);
    }
}

