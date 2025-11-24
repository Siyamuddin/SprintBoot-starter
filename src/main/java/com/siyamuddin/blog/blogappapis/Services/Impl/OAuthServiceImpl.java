package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OAuthServiceImpl implements OAuthService {
    
    @Autowired
    private UserRepo userRepo;
    
    // Store OAuth provider mappings (in production, use a separate OAuthAccount entity)
    // For now, using a simple approach - can be enhanced with a proper entity
    private final Map<String, Map<String, String>> oauthAccounts = new HashMap<>();
    
    @Override
    @Transactional
    public User linkOAuthAccount(User user, String provider, String providerId) {
        // In a real implementation, you'd have an OAuthAccount entity
        // For now, we'll just log it
        String key = user.getId() + "_" + provider;
        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put("provider", provider);
        accountInfo.put("providerId", providerId);
        oauthAccounts.put(key, accountInfo);
        
        log.info("OAuth account linked: user={}, provider={}, providerId={}", 
                user.getEmail(), provider, providerId);
        return user;
    }
    
    @Override
    @Transactional
    public void unlinkOAuthAccount(User user, String provider) {
        String key = user.getId() + "_" + provider;
        oauthAccounts.remove(key);
        log.info("OAuth account unlinked: user={}, provider={}", user.getEmail(), provider);
    }
    
    @Override
    @Transactional
    public User findOrCreateUserFromOAuth(String provider, String providerId, String email, String name) {
        // First, try to find user by email
        Optional<User> existingUser = userRepo.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            linkOAuthAccount(user, provider, providerId);
            return user;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setEmailVerified(true); // OAuth providers verify emails
        newUser.setPassword(""); // OAuth users don't have passwords
        
        User saved = userRepo.save(newUser);
        linkOAuthAccount(saved, provider, providerId);
        
        log.info("New user created from OAuth: email={}, provider={}", email, provider);
        return saved;
    }
}

