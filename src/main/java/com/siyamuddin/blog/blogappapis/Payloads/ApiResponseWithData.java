package com.siyamuddin.blog.blogappapis.Payloads;

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
public class ApiResponseWithData<T> {
    private T data;
    private String message;
    private boolean success;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public ApiResponseWithData(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }
}

