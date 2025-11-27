package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Config.Properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for rate limiting with per-user and per-IP support.
 * Buckets are stored in-memory per application instance, keyed by user/email or IP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitProperties rateLimitProperties;

    // Cache for bucket configurations (immutable Bandwidth per (requests,duration))
    private final ConcurrentMap<String, Bandwidth> bandwidthCache = new ConcurrentHashMap<>();

    // Actual buckets per key (e.g. "rate-limit:login:ip", "rate-limit:post:email")
    private final ConcurrentMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Try to consume from login rate limit bucket (per IP address).
     * @param identifier IP address or user identifier
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumeLogin(String identifier) {
        return getBucket("rate-limit:login:" + identifier, rateLimitProperties.getLogin())
                .tryConsume(1);
    }

    /**
     * Try to consume from registration rate limit bucket (per IP address).
     * @param identifier IP address
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumeRegistration(String identifier) {
        return getBucket("rate-limit:registration:" + identifier, rateLimitProperties.getRegistration())
                .tryConsume(1);
    }

    /**
     * Try to consume from post creation rate limit bucket (per user email).
     * @param identifier User email or identifier
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumePostCreation(String identifier) {
        return getBucket("rate-limit:post:" + identifier, rateLimitProperties.getPost())
                .tryConsume(1);
    }

    /**
     * Try to consume from comment creation rate limit bucket (per user email).
     * @param identifier User email or identifier
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumeCommentCreation(String identifier) {
        return getBucket("rate-limit:comment:" + identifier, rateLimitProperties.getComment())
                .tryConsume(1);
    }

    /**
     * Try to consume from password change rate limit bucket (per user email).
     * @param identifier User email or identifier
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumePasswordChange(String identifier) {
        return getBucket("rate-limit:password-change:" + identifier, rateLimitProperties.getPasswordChange())
                .tryConsume(1);
    }

    /**
     * Try to consume from general API rate limit bucket (per user email or IP).
     * @param identifier User email or IP address
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsumeGeneralApi(String identifier) {
        return getBucket("rate-limit:general:" + identifier, rateLimitProperties.getGeneral())
                .tryConsume(1);
    }

    /**
     * Try to consume from login rate limit bucket and return remaining tokens.
     * @param identifier IP address or user identifier
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingLogin(String identifier) {
        return getBucket("rate-limit:login:" + identifier, rateLimitProperties.getLogin())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Try to consume from registration rate limit bucket and return remaining tokens.
     * @param identifier IP address
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingRegistration(String identifier) {
        return getBucket("rate-limit:registration:" + identifier, rateLimitProperties.getRegistration())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Try to consume from post creation rate limit bucket and return remaining tokens.
     * @param identifier User email or identifier
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingPostCreation(String identifier) {
        return getBucket("rate-limit:post:" + identifier, rateLimitProperties.getPost())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Try to consume from comment creation rate limit bucket and return remaining tokens.
     * @param identifier User email or identifier
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingCommentCreation(String identifier) {
        return getBucket("rate-limit:comment:" + identifier, rateLimitProperties.getComment())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Try to consume from password change rate limit bucket and return remaining tokens.
     * @param identifier User email or identifier
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingPasswordChange(String identifier) {
        return getBucket("rate-limit:password-change:" + identifier, rateLimitProperties.getPasswordChange())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Try to consume from general API rate limit bucket and return remaining tokens.
     * @param identifier User email or IP address
     * @return ConsumptionProbe with remaining tokens info
     */
    public ConsumptionProbe tryConsumeAndReturnRemainingGeneralApi(String identifier) {
        return getBucket("rate-limit:general:" + identifier, rateLimitProperties.getGeneral())
                .tryConsumeAndReturnRemaining(1);
    }

    /**
     * Gets or creates a bucket for the given key (per user/IP) in memory.
     *
     * @param key    Unique identifier with prefix (e.g., "rate-limit:login:192.168.1.1")
     * @param config Rate limit configuration (requests and duration in hours)
     * @return Configured bucket
     */
    private Bucket getBucket(String key, RateLimitProperties.RateLimitConfig config) {
        String bandwidthKey = config.getRequests() + ":" + config.getDuration();

        Bandwidth limit = bandwidthCache.computeIfAbsent(
                bandwidthKey,
                k -> Bandwidth.classic(
                        config.getRequests(),
                        Refill.intervally(config.getRequests(), Duration.ofHours(config.getDuration()))
                )
        );

        return bucketCache.computeIfAbsent(
                key,
                k -> Bucket.builder().addLimit(limit).build()
        );
    }
} 