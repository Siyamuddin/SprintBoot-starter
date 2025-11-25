package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Config.Properties.SecurityProperties;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Exceptions.ResourceNotFoundException;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.AccountSecurityService;
import com.siyamuddin.blog.blogappapis.Services.AuditService;
import com.siyamuddin.blog.blogappapis.Services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private SecurityProperties securityProperties;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AuditService auditService;
    
    @Override
    @Transactional
    public void lockAccount(String email, int durationMinutes) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, durationMinutes);
        user.setAccountLockedUntil(cal.getTime());
        userRepo.save(user);
        
        emailService.sendAccountLockedEmail(user.getEmail(), user.getName(), durationMinutes);
        auditService.logSecurityEvent(user, "ACCOUNT_LOCKED", true);
        log.info("Account locked for user: {} for {} minutes", email, durationMinutes);
    }
    
    @Override
    @Transactional
    public void unlockAccount(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepo.save(user);
        
        auditService.logSecurityEvent(user, "ACCOUNT_UNLOCKED", true);
        log.info("Account unlocked for user: {}", email);
    }
    
    @Override
    @Transactional
    public void incrementFailedLoginAttempts(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);
        
        // Lock account if max attempts reached
        if (attempts >= securityProperties.getMaxFailedLoginAttempts()) {
            lockAccount(email, securityProperties.getAccountLockoutDurationMinutes());
        } else {
            userRepo.save(user);
        }
        
        log.warn("Failed login attempt {} for user: {}", attempts, email);
    }
    
    @Override
    @Transactional
    public void resetFailedLoginAttempts(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        user.setFailedLoginAttempts(0);
        userRepo.save(user);
        
        log.info("Failed login attempts reset for user: {}", email);
    }
    
    @Override
    public boolean isAccountLocked(User user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }
        return user.getAccountLockedUntil().after(new Date());
    }
}

