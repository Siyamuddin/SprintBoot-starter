package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Config.Properties.SecurityProperties;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Exceptions.ResourceNotFoundException;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.EmailService;
import com.siyamuddin.blog.blogappapis.Services.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SecurityProperties securityProperties;
    
    private static final int TOKEN_EXPIRY_HOURS = 1;
    
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", 0));
        
        String token = generateResetToken();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(new Date(System.currentTimeMillis() + TOKEN_EXPIRY_HOURS * 60 * 60 * 1000L));
        userRepo.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        log.info("Password reset email sent to user: {}", user.getEmail());
    }
    
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepo.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("User", "reset token", 0));
        
        if (user.getPasswordResetTokenExpiry() != null && 
            user.getPasswordResetTokenExpiry().before(new Date())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }
        
        // Validate password strength
        validatePassword(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepo.save(user);
        
        log.info("Password reset successful for user: {}", user.getEmail());
    }
    
    @Override
    public boolean validateResetToken(String token) {
        try {
            User user = userRepo.findByPasswordResetToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "reset token", null));
            
            if (user.getPasswordResetTokenExpiry() != null && 
                user.getPasswordResetTokenExpiry().before(new Date())) {
                return false;
            }
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    @Override
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }
    
    private void validatePassword(String password) {
        if (password == null || password.length() < securityProperties.getPasswordMinLength()) {
            throw new IllegalArgumentException("Password must be at least " + 
                securityProperties.getPasswordMinLength() + " characters long");
        }
        
        if (password.length() > securityProperties.getPasswordMaxLength()) {
            throw new IllegalArgumentException("Password must be at most " + 
                securityProperties.getPasswordMaxLength() + " characters long");
        }
        
        if (securityProperties.getPasswordRequireUppercase() && 
            !password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (securityProperties.getPasswordRequireLowercase() && 
            !password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (securityProperties.getPasswordRequireDigit() && 
            !password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        
        if (securityProperties.getPasswordRequireSpecialChar() && 
            !password.matches(".*[@#$%^&+=].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character (@#$%^&+=)");
        }
    }
}

