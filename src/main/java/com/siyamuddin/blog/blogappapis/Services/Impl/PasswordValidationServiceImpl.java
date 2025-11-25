package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Config.Properties.SecurityProperties;
import com.siyamuddin.blog.blogappapis.Services.PasswordValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PasswordValidationServiceImpl implements PasswordValidationService {
    
    @Autowired
    private SecurityProperties securityProperties;
    
    @Override
    public void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        if (password.length() < securityProperties.getPasswordMinLength()) {
            throw new IllegalArgumentException(
                String.format("Password must be at least %d characters long", 
                    securityProperties.getPasswordMinLength())
            );
        }
        
        if (password.length() > securityProperties.getPasswordMaxLength()) {
            throw new IllegalArgumentException(
                String.format("Password must be at most %d characters long", 
                    securityProperties.getPasswordMaxLength())
            );
        }
        
        // Build a list of missing requirements for better error messages
        StringBuilder missingRequirements = new StringBuilder();
        
        if (securityProperties.getPasswordRequireUppercase() && 
            !password.matches(".*[A-Z].*")) {
            missingRequirements.append("uppercase letter, ");
        }
        
        if (securityProperties.getPasswordRequireLowercase() && 
            !password.matches(".*[a-z].*")) {
            missingRequirements.append("lowercase letter, ");
        }
        
        if (securityProperties.getPasswordRequireDigit() && 
            !password.matches(".*[0-9].*")) {
            missingRequirements.append("digit, ");
        }
        
        if (securityProperties.getPasswordRequireSpecialChar() &&
            !password.matches(".*[@#$%^&+=!?*~`_\\-\\[\\]{}|\\\\:;\"'<>,./].*")) {
            missingRequirements.append("special character, ");
        }
        
        if (missingRequirements.length() > 0) {
            String requirements = missingRequirements.toString().replaceAll(", $", "");
            throw new IllegalArgumentException(
                String.format("Password must contain at least one %s", requirements)
            );
        }
    }
}

