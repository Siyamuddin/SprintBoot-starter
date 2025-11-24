package com.siyamuddin.blog.blogappapis.Config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching infrastructure only when the caching toggle is on.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.caching.enabled", havingValue = "true", matchIfMissing = true)
public class CachingToggleConfig {
}

