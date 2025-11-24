package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.User;

public interface AccountSecurityService {
    void lockAccount(String email, int durationMinutes);
    void unlockAccount(String email);
    void incrementFailedLoginAttempts(String email);
    void resetFailedLoginAttempts(String email);
    boolean isAccountLocked(User user);
}

