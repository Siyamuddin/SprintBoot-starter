package com.siyamuddin.blog.blogappapis.Config.Properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limiting configuration properties.
 * All duration values are in hours.
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private Login login = new Login();
    private Registration registration = new Registration();
    private PasswordChange passwordChange = new PasswordChange();
    private Post post = new Post();
    private Comment comment = new Comment();
    private General general = new General();
    
    @PostConstruct
    public void validate() {
        validateRateLimit("login", login);
        validateRateLimit("registration", registration);
        validateRateLimit("password-change", passwordChange);
        validateRateLimit("post", post);
        validateRateLimit("comment", comment);
        validateRateLimit("general", general);
        log.info("Rate limit properties validated successfully");
    }
    
    private void validateRateLimit(String name, RateLimitConfig config) {
        if (config.getRequests() == null || config.getRequests() <= 0) {
            throw new IllegalStateException(
                String.format("app.rate-limit.%s.requests must be greater than 0", name)
            );
        }
        if (config.getDuration() == null || config.getDuration() <= 0) {
            throw new IllegalStateException(
                String.format("app.rate-limit.%s.duration must be greater than 0 (in hours)", name)
            );
        }
    }
    
    /**
     * Base class for rate limit configurations.
     * Provides common structure for all rate limit types.
     */
    @Getter
    @Setter
    public static class RateLimitConfig {
        private Integer requests;
        private Integer duration;
    }
    
    @Getter
    @Setter
    public static class Login extends RateLimitConfig {
        public Login() {
            setRequests(10);
            setDuration(1); // hours
        }
    }
    
    @Getter
    @Setter
    public static class Registration extends RateLimitConfig {
        public Registration() {
            setRequests(10);
            setDuration(1); // hours
        }
    }
    
    @Getter
    @Setter
    public static class PasswordChange extends RateLimitConfig {
        public PasswordChange() {
            setRequests(5);
            setDuration(1); // hours
        }
    }
    
    @Getter
    @Setter
    public static class Post extends RateLimitConfig {
        public Post() {
            setRequests(10);
            setDuration(1); // hours
        }
    }
    
    @Getter
    @Setter
    public static class Comment extends RateLimitConfig {
        public Comment() {
            setRequests(20);
            setDuration(1); // hours
        }
    }
    
    @Getter
    @Setter
    public static class General extends RateLimitConfig {
        public General() {
            setRequests(50000);
            setDuration(1); // hours
        }
    }
}

