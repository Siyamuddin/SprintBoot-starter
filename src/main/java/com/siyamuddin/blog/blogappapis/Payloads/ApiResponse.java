package com.siyamuddin.blog.blogappapis.Payloads;

import com.siyamuddin.blog.blogappapis.Exceptions.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    private String message;
    private boolean success;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String errorCode;
    
    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.errorCode = null;
    }
    
    public ApiResponse(String message, boolean success, ErrorCode errorCode) {
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode != null ? errorCode.getCode() : null;
    }
    
    public ApiResponse(String message, boolean success, String errorCode) {
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
    }
}
