package com.siyamuddin.blog.blogappapis.Exceptions;

import com.siyamuddin.blog.blogappapis.Payloads.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied for request: {} - User: {} - Error: {}",
                request.getDescription(false),
                getCurrentUser(),
                ex.getMessage());

        ApiResponse apiResponse = new ApiResponse(
                "Access denied. You don't have permission to perform this action.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed for request: {} - Error: {}",
                request.getDescription(false),
                ex.getMessage());

        ApiResponse apiResponse = new ApiResponse(
                "Authentication failed. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials attempt for request: {} - IP: {}",
                request.getDescription(false),
                getClientIP(request));

        ApiResponse apiResponse = new ApiResponse(
                "Invalid username or password. Please check your credentials and try again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        log.warn("Username not found for request: {} - Username: {}",
                request.getDescription(false),
                ex.getMessage());

        ApiResponse apiResponse = new ApiResponse(
                "User not found. Please check your credentials.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    // ========== JWT EXCEPTIONS ==========

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        log.warn("Expired JWT token for request: {} - User: {}",
                request.getDescription(false),
                getCurrentUser());

        ApiResponse apiResponse = new ApiResponse(
                "Your session has expired. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse> handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        log.warn("Malformed JWT token for request: {} - IP: {}",
                request.getDescription(false),
                getClientIP(request));

        ApiResponse apiResponse = new ApiResponse(
                "Invalid authentication token. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ApiResponse> handleUnsupportedJwtException(UnsupportedJwtException ex, WebRequest request) {
        log.warn("Unsupported JWT token for request: {} - IP: {}",
                request.getDescription(false),
                getClientIP(request));

        ApiResponse apiResponse = new ApiResponse(
                "Invalid token format. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse> handleSignatureException(SignatureException ex, WebRequest request) {
        log.error("JWT signature verification failed for request: {} - IP: {}",
                request.getDescription(false),
                getClientIP(request));

        ApiResponse apiResponse = new ApiResponse(
                "Token verification failed. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument in JWT processing for request: {} - Error: {}",
                request.getDescription(false),
                ex.getMessage());

        ApiResponse apiResponse = new ApiResponse(
                "Invalid token. Please login again.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }
    // ========== EXISTING EXCEPTIONS ==========

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.info("Resource not found: {} - Request: {}",
                ex.getMessage(),
                request.getDescription(false));

        ApiResponse apiResponse = new ApiResponse(ex.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgsNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed for request: {} - Errors: {}",
                request.getDescription(false),
                ex.getBindingResult().getErrorCount());

        Map<String, String> resp = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            resp.put(fieldName, message);
        });

        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ApiResponse> resourceAlreadyExistException(UserAlreadyExists ex, WebRequest request) {
        log.warn("User already exists: {} - Request: {}",
                ex.getMessage(),
                request.getDescription(false));

        ApiResponse apiResponse = new ApiResponse(ex.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // ========== RATE LIMITING EXCEPTIONS ==========

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        log.warn("Rate limit exceeded for request: {} - IP: {} - Retry after: {} seconds",
                request.getDescription(false),
                getClientIP(request),
                ex.getRetryAfterSeconds());

        ApiResponse apiResponse = new ApiResponse(
                "Rate limit exceeded. Please try again later.",
                false
        );
        
        // Set retry headers
        jakarta.servlet.http.HttpServletResponse response = 
            ((org.springframework.web.context.request.ServletRequestAttributes) 
             org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getResponse();
        
        if (response != null) {
            response.setHeader("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
            response.setHeader("X-RateLimit-RetryAfter", String.valueOf(ex.getRetryAfterSeconds()));
        }
        
        return new ResponseEntity<>(apiResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    // ========== GENERAL EXCEPTIONS ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred for request: {} - Error: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex);

        ApiResponse apiResponse = new ApiResponse(
                "An unexpected error occurred. Please try again later.",
                false
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // ========== HELPER METHODS ==========

    private String getCurrentUser() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String getClientIP(WebRequest request) {
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
