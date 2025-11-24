package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user);
    boolean verifyEmail(String token);
    void resendVerificationEmail(String email);
    String generateVerificationToken();
}

