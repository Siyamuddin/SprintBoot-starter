package com.siyamuddin.blog.blogappapis.Config.Properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Role configuration properties.
 * Defines the role IDs used in the application.
 * These should match the role IDs in your database.
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.roles")
public class RoleProperties {
    /**
     * Admin user role ID (default: 1)
     */
    private Integer adminUser = 1;
    
    /**
     * Normal user role ID (default: 2)
     */
    private Integer normalUser = 2;
    
    @PostConstruct
    public void validate() {
        if (adminUser == null || adminUser <= 0) {
            throw new IllegalStateException(
                "app.roles.admin-user must be a positive integer"
            );
        }
        if (normalUser == null || normalUser <= 0) {
            throw new IllegalStateException(
                "app.roles.normal-user must be a positive integer"
            );
        }
        if (adminUser.equals(normalUser)) {
            throw new IllegalStateException(
                "app.roles.admin-user and app.roles.normal-user must be different"
            );
        }
        log.info("Role properties validated: admin={}, normal={}", adminUser, normalUser);
    }
}

