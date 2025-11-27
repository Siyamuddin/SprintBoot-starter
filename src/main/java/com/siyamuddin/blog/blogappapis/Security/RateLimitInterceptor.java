package com.siyamuddin.blog.blogappapis.Security;

import com.siyamuddin.blog.blogappapis.Exceptions.RateLimitExceededException;
import com.siyamuddin.blog.blogappapis.Services.RateLimitService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for rate limiting with per-user and per-IP support.
 * - Unauthenticated endpoints (login, registration): rate limited per IP address
 * - Authenticated endpoints (posts, comments, etc.): rate limited per user email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Get identifier: IP for unauthenticated endpoints, user email for authenticated endpoints
        String identifier = getIdentifier(request, requestURI, method);

        // Check rate limits based on endpoint
        if (isLoginEndpoint(requestURI, method)) {
            // Login: rate limit per IP
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingLogin(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many login attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isRegistrationEndpoint(requestURI, method)) {
            // Registration: rate limit per IP
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingRegistration(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many registration attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isPostCreationEndpoint(requestURI, method)) {
            // Post creation: rate limit per user
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingPostCreation(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many post creation attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isCommentCreationEndpoint(requestURI, method)) {
            // Comment creation: rate limit per user
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingCommentCreation(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many comment creation attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isPasswordChangeEndpoint(requestURI, method)) {
            // Password change: rate limit per user
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingPasswordChange(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many password change attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else {
            // General API rate limiting: per user if authenticated, per IP if not
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingGeneralApi(identifier);
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many requests. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        }

        return true;
    }

    /**
     * Gets the identifier for rate limiting.
     * For authenticated endpoints: returns user email from SecurityContext
     * For unauthenticated endpoints: returns IP address
     * 
     * @param request HTTP request
     * @param requestURI Request URI
     * @param method HTTP method
     * @return Identifier string (email or IP address)
     */
    private String getIdentifier(HttpServletRequest request, String requestURI, String method) {
        // For unauthenticated endpoints (login, registration, password reset), use IP
        if (isLoginEndpoint(requestURI, method) || 
            isRegistrationEndpoint(requestURI, method) ||
            isPasswordResetEndpoint(requestURI, method)) {
            return getClientIpAddress(request);
        }

        // For authenticated endpoints, try to get user email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName(); // This is the email (username in JWT)
        }

        // Fallback to IP if no authentication
        return getClientIpAddress(request);
    }

    /**
     * Extracts client IP address from request, handling proxy headers.
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress != null ? ipAddress : "unknown";
    }

    private boolean isLoginEndpoint(String uri, String method) {
        return uri.contains("/auth/login") && "POST".equals(method);
    }

    private boolean isRegistrationEndpoint(String uri, String method) {
        return uri.contains("/auth/register") && "POST".equals(method);
    }

    private boolean isPostCreationEndpoint(String uri, String method) {
        return uri.contains("/posts") && "POST".equals(method);
    }

    private boolean isCommentCreationEndpoint(String uri, String method) {
        return uri.contains("/comments") && "POST".equals(method);
    }

    private boolean isPasswordChangeEndpoint(String uri, String method) {
        return uri.contains("/change-password") && ("POST".equals(method) || "PUT".equals(method));
    }

    private boolean isPasswordResetEndpoint(String uri, String method) {
        return (uri.contains("/auth/reset-password") || uri.contains("/auth/forgot-password")) 
                && "POST".equals(method);
    }

    private void setRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        long remaining = probe.getRemainingTokens();
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        // Limit is remaining + consumed (1 token consumed in this request)
        response.setHeader("X-RateLimit-Limit", String.valueOf(remaining + 1));
    }
} 