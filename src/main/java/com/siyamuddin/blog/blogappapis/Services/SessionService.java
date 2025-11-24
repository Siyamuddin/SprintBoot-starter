package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Entity.UserSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SessionService {
    UserSession createSession(User user, HttpServletRequest request);
    void invalidateSession(String sessionId);
    void invalidateAllUserSessions(Integer userId);
    List<UserSession> getActiveSessions(Integer userId);
    void refreshSession(String sessionId);
    void cleanupExpiredSessions();
}

