package com.siyamuddin.blog.blogappapis.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to generate and track unique request IDs for each HTTP request.
 * Adds request ID to response headers and MDC for logging.
 */
@Slf4j
@Component
@Order(1) // Execute early in the filter chain
public class RequestIdFilter extends OncePerRequestFilter {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String requestId = getOrGenerateRequestId(request);
        
        // Add to MDC for logging
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        
        // Add to response header
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC after request processing
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }
    
    private String getOrGenerateRequestId(HttpServletRequest request) {
        // Check if request ID is already present in request header
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        
        if (requestId == null || requestId.trim().isEmpty()) {
            // Generate new request ID
            requestId = UUID.randomUUID().toString();
        }
        
        return requestId;
    }
}

