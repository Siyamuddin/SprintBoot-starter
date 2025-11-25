package com.siyamuddin.blog.blogappapis.Services;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    @Autowired
    @Qualifier("loginRateLimitBucket")
    private Bucket loginBucket;

    @Autowired
    @Qualifier("registrationRateLimitBucket")
    private Bucket registrationBucket;

    @Autowired
    @Qualifier("postCreationRateLimitBucket")
    private Bucket postCreationBucket;

    @Autowired
    @Qualifier("commentCreationRateLimitBucket")
    private Bucket commentCreationBucket;

    @Autowired
    @Qualifier("passwordChangeRateLimitBucket")
    private Bucket passwordChangeBucket;

    @Autowired
    @Qualifier("generalApiRateLimitBucket")
    private Bucket generalApiBucket;

    public boolean tryConsumeLogin() {
        return loginBucket.tryConsume(1);
    }

    public boolean tryConsumeRegistration() {
        return registrationBucket.tryConsume(1);
    }

    public boolean tryConsumePostCreation() {
        return postCreationBucket.tryConsume(1);
    }

    public boolean tryConsumeCommentCreation() {
        return commentCreationBucket.tryConsume(1);
    }

    public boolean tryConsumePasswordChange() {
        return passwordChangeBucket.tryConsume(1);
    }

    public boolean tryConsumeGeneralApi() {
        return generalApiBucket.tryConsume(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingLogin() {
        return loginBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingRegistration() {
        return registrationBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingPostCreation() {
        return postCreationBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingCommentCreation() {
        return commentCreationBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingPasswordChange() {
        return passwordChangeBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe tryConsumeAndReturnRemainingGeneralApi() {
        return generalApiBucket.tryConsumeAndReturnRemaining(1);
    }
} 