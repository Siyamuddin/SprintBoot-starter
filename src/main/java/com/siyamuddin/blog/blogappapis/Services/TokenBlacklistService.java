package com.siyamuddin.blog.blogappapis.Services;

public interface TokenBlacklistService {
    void blacklistToken(String token, Integer userId);
    boolean isTokenBlacklisted(String token);
    void cleanupExpiredTokens();
}

