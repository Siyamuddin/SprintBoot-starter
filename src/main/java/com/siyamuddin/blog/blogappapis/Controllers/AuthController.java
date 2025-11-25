package com.siyamuddin.blog.blogappapis.Controllers;

import com.siyamuddin.blog.blogappapis.Entity.JwtRequest;
import com.siyamuddin.blog.blogappapis.Entity.JwtResponse;
import com.siyamuddin.blog.blogappapis.Entity.RefreshToken;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Payloads.ApiResponse;
import com.siyamuddin.blog.blogappapis.Payloads.SecurityEventLogger;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import com.siyamuddin.blog.blogappapis.Repository.RefreshTokenRepo;
import com.siyamuddin.blog.blogappapis.Security.JwtHelper;
import com.siyamuddin.blog.blogappapis.Services.AccountSecurityService;
import com.siyamuddin.blog.blogappapis.Services.AuditService;
import com.siyamuddin.blog.blogappapis.Services.EmailVerificationService;
import com.siyamuddin.blog.blogappapis.Services.PasswordResetService;
import com.siyamuddin.blog.blogappapis.Services.SessionService;
import com.siyamuddin.blog.blogappapis.Services.TokenBlacklistService;
import com.siyamuddin.blog.blogappapis.Services.UserService;
import com.siyamuddin.blog.blogappapis.Config.Properties.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager manager;
    private final UserService userService;
    private final JwtHelper helper;
    private final SecurityEventLogger securityEventLogger;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final SessionService sessionService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final AuditService auditService;
    private final AccountSecurityService accountSecurityService;
    private final com.siyamuddin.blog.blogappapis.Config.MetricsConfig.BusinessMetrics businessMetrics;
    private final SecurityProperties securityProperties;

    public AuthController(
            UserDetailsService userDetailsService,
            AuthenticationManager manager,
            UserService userService,
            JwtHelper helper,
            SecurityEventLogger securityEventLogger,
            EmailVerificationService emailVerificationService,
            PasswordResetService passwordResetService,
            SessionService sessionService,
            TokenBlacklistService tokenBlacklistService,
            RefreshTokenRepo refreshTokenRepo,
            AuditService auditService,
            AccountSecurityService accountSecurityService,
            com.siyamuddin.blog.blogappapis.Config.MetricsConfig.BusinessMetrics businessMetrics,
            SecurityProperties securityProperties) {
        this.userDetailsService = userDetailsService;
        this.manager = manager;
        this.userService = userService;
        this.helper = helper;
        this.securityEventLogger = securityEventLogger;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
        this.sessionService = sessionService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.auditService = auditService;
        this.accountSecurityService = accountSecurityService;
        this.businessMetrics = businessMetrics;
        this.securityProperties = securityProperties;
    }

    @Operation(
        summary = "User login",
        description = "Authenticate user with email and password. Returns JWT access token and refresh token."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Invalid credentials or email not verified"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429", 
            description = "Too many login attempts"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest request, HttpServletRequest httpRequest) {
        businessMetrics.incrementLoginAttempts();
        io.micrometer.core.instrument.Timer.Sample sample = businessMetrics.startLoginTimer();
        try {
            this.doAuthenticate(request.getEmail(), request.getPassword());

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            User user = userService.getUserEntityByEmail(request.getEmail());

            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("Account is locked. Please try again later.");
            }

            // Check if email is verified (only if required by configuration)
            if (securityProperties.getRequireEmailVerificationForLogin() != null && 
                securityProperties.getRequireEmailVerificationForLogin() &&
                (user.getEmailVerified() == null || !user.getEmailVerified())) {
                throw new BadCredentialsException("Email not verified. Please verify your email before logging in.");
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
            userService.updateUserLastLogin(user);

            // Log successful login
            securityEventLogger.logLoginAttempt(request.getEmail(), getClientIP(httpRequest), true);
            auditService.logSecurityEvent(user, "LOGIN_SUCCESS", true);
            
            // Metrics
            businessMetrics.incrementLoginSuccess();
            businessMetrics.incrementActiveSessions();
            businessMetrics.recordLoginDuration(sample);

            JwtResponse response = JwtResponse.builder()
                    .jwtToken(accessToken)
                    .refreshToken(refreshTokenString)
                    .username(userDetails.getUsername())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BadCredentialsException e) {
            // Handle failed login attempts
            businessMetrics.incrementLoginFailure();
            businessMetrics.recordLoginDuration(sample);
            try {
                accountSecurityService.incrementFailedLoginAttempts(request.getEmail());
                
                try {
                    User user = userService.getUserEntityByEmail(request.getEmail());
                    auditService.logSecurityEvent(user, "LOGIN_FAILED", false);
                } catch (Exception ex) {
                    // User not found, skip audit
                    log.debug("User not found for failed login audit: {}", request.getEmail());
                }
            } catch (Exception ex) {
                log.error("Error handling failed login", ex);
            }
            
            securityEventLogger.logLoginAttempt(request.getEmail(), getClientIP(httpRequest), false);
            throw e;
        }
    }

    // Use HttpUtils for IP extraction
    private String getClientIP(HttpServletRequest request) {
        return com.siyamuddin.blog.blogappapis.Utils.HttpUtils.getClientIP(request);
    }

    private void doAuthenticate(String email, String password) {
        log.debug("Attempting authentication for email: {}", email);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);
            log.debug("Authentication successful for email: {}", email);
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for email: {} - {}", email, e.getMessage());
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email: {}", email, e);
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }
    }

    @Operation(
        summary = "User registration",
        description = "Register a new user account. Verification email will be sent."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid input or user already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429", 
            description = "Too many registration attempts"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @org.springframework.validation.annotation.Validated(com.siyamuddin.blog.blogappapis.Payloads.UserPayload.ValidationGroups.Create.class) @RequestBody UserDto userDto, HttpServletRequest request) {
        io.micrometer.core.instrument.Timer.Sample sample = businessMetrics.startRegistrationTimer();
        UserDto registeredUser = this.userService.registerNewUser(userDto);
        
        // Send verification email
        User user = userService.getUserEntityByEmail(registeredUser.getEmail());
        emailVerificationService.sendVerificationEmail(user);
        
        // Metrics
        businessMetrics.incrementRegistration();
        businessMetrics.recordRegistrationDuration(sample);
        
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
    
    @Operation(
        summary = "Verify email address",
        description = "Verify user email using the verification token sent via email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Email verified successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid or expired token"
        )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam @NotBlank(message = "Token is required") String token) {
        io.micrometer.core.instrument.Timer.Sample sample = businessMetrics.startEmailVerificationTimer();
        boolean verified = emailVerificationService.verifyEmail(token);
        businessMetrics.recordEmailVerificationDuration(sample);
        if (verified) {
            businessMetrics.incrementEmailVerification();
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
    
    @Operation(
        summary = "Resend verification email",
        description = "Resend email verification token to user's email address"
    )
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(
            @Parameter(description = "User email address", required = true)
            @RequestParam @NotBlank @Email String email) {
        emailVerificationService.resendVerificationEmail(email);
        return new ResponseEntity<>(
            new ApiResponse("Verification email sent", true), 
            HttpStatus.OK
        );
    }
    
    @Operation(
        summary = "Request password reset",
        description = "Request password reset email. Always returns success to prevent email enumeration."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Parameter(description = "User email address", required = true)
            @RequestParam @NotBlank @Email String email) {
        passwordResetService.requestPasswordReset(email);
        businessMetrics.incrementPasswordResetRequest();
        return new ResponseEntity<>(
            new ApiResponse("Password reset email sent if account exists", true), 
            HttpStatus.OK
        );
    }
    
    @Operation(
        summary = "Reset password",
        description = "Reset user password using the reset token from email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Password reset successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid token, expired token, or weak password"
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Parameter(description = "Password reset token", required = true)
            @RequestParam @NotBlank(message = "Token is required") String token,
            @Parameter(description = "New password (must meet strength requirements)", required = true)
            @RequestParam @NotBlank(message = "New password is required") String newPassword) {
        io.micrometer.core.instrument.Timer.Sample sample = businessMetrics.startPasswordResetTimer();
        passwordResetService.resetPassword(token, newPassword);
        businessMetrics.incrementPasswordResetSuccess();
        businessMetrics.recordPasswordResetDuration(sample);
        return new ResponseEntity<>(
            new ApiResponse("Password reset successfully", true), 
            HttpStatus.OK
        );
    }
    
    @Operation(
        summary = "Refresh access token",
        description = "Get a new access token using a valid refresh token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Invalid or expired refresh token"
        )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam @NotBlank(message = "Refresh token is required") String refreshToken) {
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
    
    @Operation(
        summary = "User logout",
        description = "Logout user and invalidate all tokens and sessions"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Logged out successfully"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @Parameter(description = "Bearer token", required = false)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = helper.getUsernameFromToken(token);
                try {
                    User user = userService.getUserEntityByEmail(email);
                    tokenBlacklistService.blacklistToken(token, user.getId());
                    sessionService.invalidateAllUserSessions(user.getId());
                    refreshTokenRepo.revokeAllUserTokens(user);
                    auditService.logSecurityEvent(user, "LOGOUT", true);
                } catch (Exception ex) {
                    log.warn("User not found for logout: {}", email);
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
