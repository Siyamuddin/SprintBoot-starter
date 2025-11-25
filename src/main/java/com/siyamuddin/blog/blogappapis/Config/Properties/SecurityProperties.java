package com.siyamuddin.blog.blogappapis.Config.Properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Security configuration properties.
 * Validates security settings on application startup.
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private Integer maxFailedLoginAttempts = 5;
    private Integer accountLockoutDurationMinutes = 30;
    private Integer passwordMinLength = 8;
    private Integer passwordMaxLength = 128;
    private Boolean passwordRequireUppercase = true;
    private Boolean passwordRequireLowercase = true;
    private Boolean passwordRequireDigit = true;
    private Boolean passwordRequireSpecialChar = true;
    private Integer passwordHistoryCount = 5; // Prevent reuse of last N passwords
    private Integer sessionTimeoutMinutes = 30;
    
    // Token expiry settings (in hours)
    private Integer emailVerificationTokenExpiryHours = 24;
    private Integer passwordResetTokenExpiryHours = 1;
    
    // Email verification requirement for login
    private Boolean requireEmailVerificationForLogin = true;
    
    @PostConstruct
    public void validate() {
        if (maxFailedLoginAttempts == null || maxFailedLoginAttempts <= 0) {
            throw new IllegalStateException(
                "app.security.max-failed-login-attempts must be greater than 0"
            );
        }
        if (accountLockoutDurationMinutes == null || accountLockoutDurationMinutes <= 0) {
            throw new IllegalStateException(
                "app.security.account-lockout-duration-minutes must be greater than 0"
            );
        }
        if (passwordMinLength == null || passwordMinLength < 4) {
            throw new IllegalStateException(
                "app.security.password-min-length must be at least 4"
            );
        }
        if (passwordMaxLength == null || passwordMaxLength < passwordMinLength) {
            throw new IllegalStateException(
                "app.security.password-max-length must be greater than or equal to password-min-length"
            );
        }
        if (passwordHistoryCount == null || passwordHistoryCount < 0) {
            throw new IllegalStateException(
                "app.security.password-history-count must be non-negative"
            );
        }
        if (emailVerificationTokenExpiryHours == null || emailVerificationTokenExpiryHours <= 0) {
            throw new IllegalStateException(
                "app.security.email-verification-token-expiry-hours must be greater than 0"
            );
        }
        if (passwordResetTokenExpiryHours == null || passwordResetTokenExpiryHours <= 0) {
            throw new IllegalStateException(
                "app.security.password-reset-token-expiry-hours must be greater than 0"
            );
        }
        log.info("Security properties validated successfully");
    }
}

