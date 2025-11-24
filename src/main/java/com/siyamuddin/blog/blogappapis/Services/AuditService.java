package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.AuditLog;
import com.siyamuddin.blog.blogappapis.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {
    void logUserAction(User user, String action, String resourceType, Object resourceId);
    void logSecurityEvent(User user, String event, boolean success);
    void logUserAction(String ipAddress, String userAgent, Integer userId, String action, 
                      String resourceType, Object resourceId, boolean success, String errorMessage);
    Page<AuditLog> getUserAuditLogs(Integer userId, Pageable pageable);
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    List<AuditLog> getUserActionsSince(Integer userId, int hours);
}

