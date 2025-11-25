package com.siyamuddin.blog.blogappapis.Config.Properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private Long accessTokenValidity = 15 * 60L; // 15 minutes in seconds
    private Long refreshTokenValidity = 7 * 24 * 60 * 60L; // 7 days in seconds
    
    @PostConstruct
    public void validate() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret is required. Please set app.jwt.secret or JWT_SECRET environment variable. " +
                "The secret must be at least 256 bits (32 characters) for HS512 algorithm."
            );
        }
        
        // HS512 requires at least 256 bits (32 characters)
        if (secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters long for HS512 algorithm. " +
                "Current length: " + secret.length()
            );
        }
        
        log.info("JWT properties validated successfully");
    }
}

