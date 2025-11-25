package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates critical environment variables and configuration on application startup.
 * Fails fast if required variables are missing.
 */
@Slf4j
@Component
public class EnvironmentValidator implements CommandLineRunner {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private JwtProperties jwtProperties;
    
    @Override
    public void run(String... args) {
        log.info("Validating environment configuration...");
        
        boolean hasErrors = false;
        
        // Validate JWT secret (already validated in JwtProperties, but log here)
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().trim().isEmpty()) {
            log.error("CRITICAL: JWT_SECRET environment variable is required but not set!");
            hasErrors = true;
        } else if (jwtProperties.getSecret().length() < 32) {
            log.error("CRITICAL: JWT_SECRET must be at least 32 characters long!");
            hasErrors = true;
        } else {
            log.info("JWT secret validated successfully");
        }
        
        // Check for production profile and validate required variables
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        if ("prod".equals(activeProfile)) {
            log.info("Production profile detected - validating production requirements...");
            
            // Validate database configuration
            String dbUrl = environment.getProperty("spring.datasource.url");
            if (dbUrl == null || dbUrl.contains("localhost")) {
                log.warn("WARNING: Database URL appears to be using localhost in production!");
            }
            
            // Validate email configuration
            String mailHost = environment.getProperty("spring.mail.host");
            if (mailHost == null || mailHost.isEmpty()) {
                log.warn("WARNING: Email host not configured. Email functionality may not work.");
            }
            
            // Validate CORS configuration
            String corsOrigins = environment.getProperty("APP_CORS_ALLOWED_ORIGINS");
            if (corsOrigins == null || corsOrigins.isEmpty()) {
                log.warn("WARNING: APP_CORS_ALLOWED_ORIGINS not set. CORS may not work correctly.");
            }
        }
        
        // Validate Redis configuration (if caching is enabled)
        String cachingEnabled = environment.getProperty("app.caching.enabled", "true");
        if ("true".equals(cachingEnabled)) {
            String redisHost = environment.getProperty("spring.data.redis.host");
            if (redisHost == null || redisHost.isEmpty()) {
                log.warn("WARNING: Redis host not configured but caching is enabled. Caching may not work.");
            }
        }
        
        if (hasErrors) {
            log.error("Environment validation failed. Please fix the errors above and restart the application.");
            throw new IllegalStateException("Environment validation failed. Check logs for details.");
        }
        
        log.info("Environment validation completed successfully");
    }
}

