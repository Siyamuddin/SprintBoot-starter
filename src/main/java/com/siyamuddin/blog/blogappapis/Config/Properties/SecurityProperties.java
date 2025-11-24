package com.siyamuddin.blog.blogappapis.Config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
}

