package com.siyamuddin.blog.blogappapis.Config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    private String from = "noreply@saasstarter.com";
    private String fromName = "SAAS Starter";
    private String verificationBaseUrl = "http://localhost:9090/api/v1/auth/verify-email";
    private String passwordResetBaseUrl = "http://localhost:9090/api/v1/auth/reset-password";
    private Boolean enabled = true;
}

