package com.siyamuddin.blog.blogappapis.Exceptions;

/**
 * Error codes for API responses.
 * Provides standardized error codes for different error scenarios.
 */
public enum ErrorCode {
    // Authentication & Authorization Errors (1000-1999)
    AUTH_INVALID_CREDENTIALS("AUTH_1001", "Invalid username or password"),
    AUTH_EMAIL_NOT_VERIFIED("AUTH_1002", "Email not verified"),
    AUTH_ACCOUNT_LOCKED("AUTH_1003", "Account is locked"),
    AUTH_TOKEN_EXPIRED("AUTH_1004", "Token has expired"),
    AUTH_TOKEN_INVALID("AUTH_1005", "Invalid token"),
    AUTH_TOKEN_BLACKLISTED("AUTH_1006", "Token has been revoked"),
    AUTH_INSUFFICIENT_PERMISSIONS("AUTH_1007", "Insufficient permissions"),
    
    // User Management Errors (2000-2999)
    USER_NOT_FOUND("USER_2001", "User not found"),
    USER_ALREADY_EXISTS("USER_2002", "User already exists"),
    USER_UPDATE_FAILED("USER_2003", "Failed to update user"),
    USER_DELETE_FAILED("USER_2004", "Failed to delete user"),
    
    // Validation Errors (3000-3999)
    VALIDATION_FAILED("VAL_3001", "Validation failed"),
    VALIDATION_PASSWORD_WEAK("VAL_3002", "Password does not meet strength requirements"),
    VALIDATION_EMAIL_INVALID("VAL_3003", "Invalid email format"),
    VALIDATION_REQUIRED_FIELD("VAL_3004", "Required field is missing"),
    
    // Password Errors (4000-4999)
    PASSWORD_RESET_TOKEN_INVALID("PWD_4001", "Invalid or expired password reset token"),
    PASSWORD_RESET_TOKEN_EXPIRED("PWD_4002", "Password reset token has expired"),
    PASSWORD_CURRENT_INCORRECT("PWD_4003", "Current password is incorrect"),
    PASSWORD_HISTORY_VIOLATION("PWD_4004", "Password cannot be reused"),
    
    // Email Errors (5000-5999)
    EMAIL_VERIFICATION_TOKEN_INVALID("EMAIL_5001", "Invalid or expired verification token"),
    EMAIL_ALREADY_VERIFIED("EMAIL_5002", "Email is already verified"),
    EMAIL_SEND_FAILED("EMAIL_5003", "Failed to send email"),
    
    // Rate Limiting Errors (6000-6999)
    RATE_LIMIT_EXCEEDED("RATE_6001", "Rate limit exceeded"),
    
    // General Errors (9000-9999)
    INTERNAL_ERROR("GEN_9001", "Internal server error"),
    RESOURCE_NOT_FOUND("GEN_9002", "Resource not found"),
    BAD_REQUEST("GEN_9003", "Bad request"),
    UNAUTHORIZED("GEN_9004", "Unauthorized"),
    FORBIDDEN("GEN_9005", "Forbidden");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

