package com.siyamuddin.blog.blogappapis.Services;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    boolean validateResetToken(String token);
    String generateResetToken();
}

