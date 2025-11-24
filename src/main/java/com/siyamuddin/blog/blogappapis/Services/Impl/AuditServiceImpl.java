package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Entity.AuditLog;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Repository.AuditLogRepo;
import com.siyamuddin.blog.blogappapis.Services.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {
    
    @Autowired
    private AuditLogRepo auditLogRepo;
    
    @Override
    @Async
    public void logUserAction(User user, String action, String resourceType, Object resourceId) {
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIP(request) : "unknown";
        String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";
        
        logUserAction(ipAddress, userAgent, user.getId(), action, resourceType, resourceId, true, null);
    }
    
    @Override
    @Async
    public void logSecurityEvent(User user, String event, boolean success) {
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIP(request) : "unknown";
        String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";
        
        logUserAction(ipAddress, userAgent, user.getId(), event, "SECURITY", null, success, null);
    }
    
    @Override
    @Async
    public void logUserAction(String ipAddress, String userAgent, Integer userId, String action, 
                             String resourceType, Object resourceId, boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId != null ? resourceId.toString() : null);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setTimestamp(LocalDateTime.now());
            
            auditLogRepo.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
    
    @Override
    public Page<AuditLog> getUserAuditLogs(Integer userId, Pageable pageable) {
        return auditLogRepo.findByUserId(userId, pageable);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepo.findByAction(action, pageable);
    }
    
    @Override
    public List<AuditLog> getUserActionsSince(Integer userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepo.findUserActionsSince(userId, since);
    }
    
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

