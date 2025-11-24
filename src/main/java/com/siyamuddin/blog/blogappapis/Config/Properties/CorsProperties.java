package com.siyamuddin.blog.blogappapis.Config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /**
     * Allowed CORS origins. Can be configured via list in properties file
     * or via APP_CORS_ALLOWED_ORIGINS environment variable (comma-separated).
     * Note: Environment variable takes precedence and is handled in SecurityConfig.
     * For properties files, use array notation:
     * app.cors.allowed-origins[0]=http://localhost:3000
     * app.cors.allowed-origins[1]=http://localhost:4200
     * Or use comma-separated format via environment variable.
     */
    private List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:8080",
            "http://localhost:5173"
    ));
    
    /**
     * Allowed HTTP methods for CORS
     */
    private List<String> allowedMethods = new ArrayList<>(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    /**
     * Allowed headers for CORS
     */
    private List<String> allowedHeaders = new ArrayList<>(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    ));
    
    /**
     * Whether credentials (cookies, authorization headers) are allowed
     */
    private Boolean allowCredentials = true;
    
    /**
     * Max age for preflight requests in seconds
     */
    private Long maxAge = 3600L; // 1 hour
}

