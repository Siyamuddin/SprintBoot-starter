package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for rate limiting buckets.
 * Uses RateLimitProperties for centralized configuration management.
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;

    @Bean
    public Bucket loginRateLimitBucket() {
        return createBucket(rateLimitProperties.getLogin());
    }

    @Bean
    public Bucket registrationRateLimitBucket() {
        return createBucket(rateLimitProperties.getRegistration());
    }

    @Bean
    public Bucket passwordChangeRateLimitBucket() {
        return createBucket(rateLimitProperties.getPasswordChange());
    }

    @Bean
    public Bucket postCreationRateLimitBucket() {
        return createBucket(rateLimitProperties.getPost());
    }

    @Bean
    public Bucket commentCreationRateLimitBucket() {
        return createBucket(rateLimitProperties.getComment());
    }

    @Bean
    public Bucket generalApiRateLimitBucket() {
        return createBucket(rateLimitProperties.getGeneral());
    }

    /**
     * Creates a rate limit bucket from configuration.
     * 
     * @param config Rate limit configuration (requests and duration in hours)
     * @return Configured bucket
     */
    private Bucket createBucket(RateLimitProperties.RateLimitConfig config) {
        Bandwidth limit = Bandwidth.classic(
            config.getRequests(),
            Refill.intervally(config.getRequests(), Duration.ofHours(config.getDuration()))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}