package com.siyamuddin.blog.blogappapis.Utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Utility class for HTTP-related operations
 */
public class HttpUtils {
    
    /**
     * Extracts the client IP address from HttpServletRequest
     * Handles X-Forwarded-For header for proxied requests
     * 
     * @param request HttpServletRequest
     * @return Client IP address
     */
    public static String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Extracts the client IP address from WebRequest
     * Handles X-Forwarded-For header for proxied requests
     * 
     * @param request WebRequest
     * @return Client IP address
     */
    public static String getClientIP(WebRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }

            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}

