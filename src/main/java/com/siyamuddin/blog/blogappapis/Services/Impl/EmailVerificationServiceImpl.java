package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Exceptions.ResourceNotFoundException;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.EmailService;
import com.siyamuddin.blog.blogappapis.Services.EmailVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private EmailService emailService;
    
    private static final int TOKEN_EXPIRY_HOURS = 24;
    
    @Override
    @Transactional
    public void sendVerificationEmail(User user) {
        String token = generateVerificationToken();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(new Date(System.currentTimeMillis() + TOKEN_EXPIRY_HOURS * 60 * 60 * 1000L));
        userRepo.save(user);
        
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);
        log.info("Verification email sent to user: {}", user.getEmail());
    }
    
    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepo.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("User", "verification token", 0));
        
        if (user.getEmailVerificationTokenExpiry() != null && 
            user.getEmailVerificationTokenExpiry().before(new Date())) {
            log.warn("Verification token expired for user: {}", user.getEmail());
            return false;
        }
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepo.save(user);
        
        log.info("Email verified for user: {}", user.getEmail());
        return true;
    }
    
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", 0));
        
        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }
        
        sendVerificationEmail(user);
    }
    
    @Override
    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}

