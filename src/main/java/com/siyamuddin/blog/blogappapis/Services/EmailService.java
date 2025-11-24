package com.siyamuddin.blog.blogappapis.Services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendWelcomeEmail(String to, String name);
    void sendVerificationEmail(String to, String name, String verificationToken);
    void sendPasswordResetEmail(String to, String name, String resetToken);
    void sendAccountLockedEmail(String to, String name, int lockoutDurationMinutes);
}

