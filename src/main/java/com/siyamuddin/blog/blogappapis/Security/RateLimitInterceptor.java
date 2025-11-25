package com.siyamuddin.blog.blogappapis.Security;

import com.siyamuddin.blog.blogappapis.Exceptions.RateLimitExceededException;
import com.siyamuddin.blog.blogappapis.Services.RateLimitService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Check rate limits based on endpoint
        if (isLoginEndpoint(requestURI, method)) {
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingLogin();
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many login attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isRegistrationEndpoint(requestURI, method)) {
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingRegistration();
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many registration attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isPostCreationEndpoint(requestURI, method)) {
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingPostCreation();
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many post creation attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isCommentCreationEndpoint(requestURI, method)) {
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingCommentCreation();
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many comment creation attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else if (isPasswordChangeEndpoint(requestURI, method)) {
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingPasswordChange();
            if (!probe.isConsumed()) {
                throw new RateLimitExceededException(
                    "Too many password change attempts. Please try again later.",
                    probe.getNanosToWaitForRefill() / 1_000_000_000
                );
            }
            setRateLimitHeaders(response, probe);
        } else {
            // General API rate limiting
            ConsumptionProbe probe = rateLimitService.tryConsumeAndReturnRemainingGeneralApi();
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
        return uri.contains("/change-password") && "POST".equals(method);
    }

    private void setRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Limit", String.valueOf(probe.getRemainingTokens() + 1));
    }
} 