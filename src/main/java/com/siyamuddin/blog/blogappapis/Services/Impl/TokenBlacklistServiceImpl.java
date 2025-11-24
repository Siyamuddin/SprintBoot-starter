package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Entity.TokenBlacklist;
import com.siyamuddin.blog.blogappapis.Repository.TokenBlacklistRepo;
import com.siyamuddin.blog.blogappapis.Security.JwtHelper;
import com.siyamuddin.blog.blogappapis.Services.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    
    @Autowired
    private TokenBlacklistRepo tokenBlacklistRepo;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    @Override
    @Transactional
    public void blacklistToken(String token, Integer userId) {
        try {
            // Get token expiration from JWT
            java.util.Date expiration = jwtHelper.getExpirationDateFromToken(token);
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                expiration.toInstant(), 
                java.time.ZoneId.systemDefault()
            );
            
            TokenBlacklist blacklistEntry = new TokenBlacklist();
            blacklistEntry.setToken(token);
            blacklistEntry.setUserId(userId);
            blacklistEntry.setExpiresAt(expiresAt);
            
            tokenBlacklistRepo.save(blacklistEntry);
            log.info("Token blacklisted for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepo.existsByToken(token);
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepo.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired tokens cleaned up from blacklist");
    }
}

