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
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
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
    
    // Actuator endpoints (public for health, authenticated for others)
    private static final String[] ACTUATOR_PUBLIC_URLS = {
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/prometheus"
    };
    
    // Swagger URLs (only enabled when springdoc is enabled)
    private static final String[] SWAGGER_URLS = {
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/v2/api-docs/**",
            "/swagger-resources/**",
            "/swagger-resources",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
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
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${springdoc.api-docs.enabled:true}")
    private boolean swaggerApiDocsEnabled;
    
    @Value("${springdoc.swagger-ui.enabled:true}")
    private boolean swaggerUiEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicUrls = getPublicUrls();
        
        log.info("Configuring security with {} public URLs", publicUrls.length);
        if (log.isDebugEnabled()) {
            log.debug("Public URLs: {}", Arrays.toString(publicUrls));
        }
        
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
        
        // Add Actuator public endpoints
        urls.addAll(Arrays.asList(ACTUATOR_PUBLIC_URLS));
        
        // Add Swagger URLs only if Swagger is enabled
        // Check both api-docs and swagger-ui enabled flags
        if (swaggerApiDocsEnabled || swaggerUiEnabled) {
            urls.addAll(Arrays.asList(SWAGGER_URLS));
            log.debug("Swagger URLs enabled. API Docs: {}, Swagger UI: {}", 
                    swaggerApiDocsEnabled, swaggerUiEnabled);
        } else {
            log.debug("Swagger URLs disabled. API Docs: {}, Swagger UI: {}", 
                    swaggerApiDocsEnabled, swaggerUiEnabled);
        }
        
        return urls.toArray(new String[0]);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from properties or environment variable
        List<String> allowedOrigins = getCorsAllowedOrigins();
        
        // Validate CORS configuration
        if (allowedOrigins.isEmpty()) {
            log.warn("No CORS origins configured. CORS may not work correctly. " +
                    "Set APP_CORS_ALLOWED_ORIGINS environment variable or configure in properties.");
        } else {
            log.info("CORS configured with {} allowed origin(s)", allowedOrigins.size());
        }
        
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Set allowed methods from properties
        List<String> allowedMethods = corsProperties.getAllowedMethods();
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            // Default methods if not configured
            allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
            log.warn("CORS allowed methods not configured, using defaults: {}", allowedMethods);
        }
        configuration.setAllowedMethods(allowedMethods);
        
        // Set allowed headers from properties
        List<String> allowedHeaders = corsProperties.getAllowedHeaders();
        if (allowedHeaders == null || allowedHeaders.isEmpty()) {
            // Default headers if not configured
            allowedHeaders = Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-Request-ID");
            log.warn("CORS allowed headers not configured, using defaults: {}", allowedHeaders);
        }
        configuration.setAllowedHeaders(allowedHeaders);
        
        // Set exposed headers
        configuration.setExposedHeaders(Arrays.asList("X-Request-ID", "X-Total-Count"));
        
        // Set credentials from properties
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        
        // Set max age from properties (default to 3600 seconds if not set)
        Long maxAge = corsProperties.getMaxAge();
        if (maxAge == null || maxAge <= 0) {
            maxAge = 3600L;
            log.warn("CORS max age not configured, using default: {} seconds", maxAge);
        }
        configuration.setMaxAge(maxAge);
        
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
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
