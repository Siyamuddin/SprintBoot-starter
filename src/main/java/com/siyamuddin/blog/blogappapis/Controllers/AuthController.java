package com.siyamuddin.blog.blogappapis.Controllers;

import com.siyamuddin.blog.blogappapis.Entity.JwtRequest;
import com.siyamuddin.blog.blogappapis.Entity.JwtResponse;
import com.siyamuddin.blog.blogappapis.Entity.RefreshToken;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Payloads.ApiResponse;
import com.siyamuddin.blog.blogappapis.Payloads.SecurityEventLogger;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import com.siyamuddin.blog.blogappapis.Repository.RefreshTokenRepo;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Security.JwtHelper;
import com.siyamuddin.blog.blogappapis.Services.AccountSecurityService;
import com.siyamuddin.blog.blogappapis.Services.AuditService;
import com.siyamuddin.blog.blogappapis.Services.EmailVerificationService;
import com.siyamuddin.blog.blogappapis.Services.PasswordResetService;
import com.siyamuddin.blog.blogappapis.Services.SessionService;
import com.siyamuddin.blog.blogappapis.Services.TokenBlacklistService;
import com.siyamuddin.blog.blogappapis.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtHelper helper;
    
    @Autowired
    private SecurityEventLogger securityEventLogger;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private RefreshTokenRepo refreshTokenRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AccountSecurityService accountSecurityService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request, HttpServletRequest httpRequest) {
        try {
            this.doAuthenticate(request.getEmail(), request.getPassword());

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("Account is locked. Please try again later.");
            }

            // Generate tokens
            String accessToken = this.helper.generateToken(userDetails);
            String refreshTokenString = this.helper.generateRefreshTokenString();

            // Save refresh token
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(refreshTokenString);
            refreshToken.setUser(user);
            refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
            refreshToken.setIsRevoked(false);
            refreshTokenRepo.save(refreshToken);

            // Create session
            sessionService.createSession(user, httpRequest);

            // Update last login
            user.setLastLoginDate(new Date());
            user.setFailedLoginAttempts(0);
            userRepo.save(user);

            // Log successful login
            securityEventLogger.logLoginAttempt(request.getEmail(), getClientIP(httpRequest), true);
            auditService.logSecurityEvent(user, "LOGIN_SUCCESS", true);

            JwtResponse response = JwtResponse.builder()
                    .jwtToken(accessToken)
                    .refreshToken(refreshTokenString)
                    .username(userDetails.getUsername())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BadCredentialsException e) {
            // Handle failed login attempts
            try {
                accountSecurityService.incrementFailedLoginAttempts(request.getEmail());
                
                User user = userRepo.findByEmail(request.getEmail()).orElse(null);
                if (user != null) {
                    auditService.logSecurityEvent(user, "LOGIN_FAILED", false);
                }
            } catch (Exception ex) {
                log.error("Error handling failed login", ex);
            }
            
            securityEventLogger.logLoginAttempt(request.getEmail(), getClientIP(httpRequest), false);
            throw e;
        }
    }

    // Add this helper method:
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto, HttpServletRequest request) {
        UserDto registeredUser = this.userService.registerNewUser(userDto);
        
        // Send verification email
        User user = userRepo.findByEmail(registeredUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after registration"));
        emailVerificationService.sendVerificationEmail(user);
        
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        boolean verified = emailVerificationService.verifyEmail(token);
        if (verified) {
            return new ResponseEntity<>(
                new ApiResponse("Email verified successfully", true), 
                HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                new ApiResponse("Invalid or expired verification token", false), 
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);
        return new ResponseEntity<>(
            new ApiResponse("Verification email sent", true), 
            HttpStatus.OK
        );
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        return new ResponseEntity<>(
            new ApiResponse("Password reset email sent if account exists", true), 
            HttpStatus.OK
        );
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token, 
            @RequestParam String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
        return new ResponseEntity<>(
            new ApiResponse("Password reset successfully", true), 
            HttpStatus.OK
        );
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestParam String refreshToken) {
        RefreshToken token = refreshTokenRepo.findByTokenAndIsRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token has expired");
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getEmail());
        String newAccessToken = helper.generateToken(userDetails);
        
        JwtResponse response = JwtResponse.builder()
                .jwtToken(newAccessToken)
                .refreshToken(refreshToken)
                .username(userDetails.getUsername())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = helper.getUsernameFromToken(token);
                User user = userRepo.findByEmail(email).orElse(null);
                if (user != null) {
                    tokenBlacklistService.blacklistToken(token, user.getId());
                    sessionService.invalidateAllUserSessions(user.getId());
                    refreshTokenRepo.revokeAllUserTokens(user);
                    auditService.logSecurityEvent(user, "LOGOUT", true);
                }
            } catch (Exception e) {
                log.error("Error during logout", e);
            }
        }
        
        return new ResponseEntity<>(
            new ApiResponse("Logged out successfully", true), 
            HttpStatus.OK
        );
    }
}
