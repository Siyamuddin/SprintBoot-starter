package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.CorsProperties;
import com.siyamuddin.blog.blogappapis.Security.JwtAuthenticationEntryPoint;
import com.siyamuddin.blog.blogappapis.Security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    // Base public URLs (always public)
    private static final String[] BASE_PUBLIC_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };
    
    // Swagger URLs (only enabled when springdoc is enabled)
    private static final String[] SWAGGER_URLS = {
            "/v3/api-docs/**",
            "/v2/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };
    
    @Autowired
    private JwtAuthenticationEntryPoint point;
    
    @Autowired
    private JwtAuthenticationFilter filter;
    
    @Autowired
    private CorsProperties corsProperties;
    
    @Autowired
    private Environment environment;
    
    @Value("${springdoc.api-docs.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicUrls = getPublicUrls();
        
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicUrls).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    private String[] getPublicUrls() {
        List<String> urls = new ArrayList<>(Arrays.asList(BASE_PUBLIC_URLS));
        
        // Add Swagger URLs only if Swagger is enabled
        if (swaggerEnabled) {
            urls.addAll(Arrays.asList(SWAGGER_URLS));
        }
        
        return urls.toArray(new String[0]);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from properties or environment variable
        List<String> allowedOrigins = getCorsAllowedOrigins();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Set allowed methods from properties
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        
        // Set allowed headers from properties
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        
        // Set credentials from properties
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        
        // Set max age from properties
        configuration.setMaxAge(corsProperties.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    private List<String> getCorsAllowedOrigins() {
        // Check if CORS origins are provided via environment variable (comma-separated)
        String envOrigins = environment.getProperty("APP_CORS_ALLOWED_ORIGINS");
        
        if (envOrigins != null && !envOrigins.trim().isEmpty()) {
            // Split by comma and trim each origin
            List<String> origins = new ArrayList<>();
            for (String origin : envOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    origins.add(trimmed);
                }
            }
            if (!origins.isEmpty()) {
                return origins;
            }
        }
        
        // Fall back to properties file configuration
        return corsProperties.getAllowedOrigins();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
