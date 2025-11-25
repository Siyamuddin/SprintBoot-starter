package com.siyamuddin.blog.blogappapis.Controllers;

import com.siyamuddin.blog.blogappapis.Config.AppConstants;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Entity.UserSession;
import com.siyamuddin.blog.blogappapis.Payloads.ApiResponse;
import com.siyamuddin.blog.blogappapis.Payloads.PagedResponse;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.ValidationGroups;
import com.siyamuddin.blog.blogappapis.Services.AuditService;
import com.siyamuddin.blog.blogappapis.Services.PasswordValidationService;
import com.siyamuddin.blog.blogappapis.Services.SessionService;
import com.siyamuddin.blog.blogappapis.Services.UserProfilePhotoService;
import com.siyamuddin.blog.blogappapis.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "JWT-Auth")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final PasswordValidationService passwordValidationService;
    private final AuditService auditService;
    private final com.siyamuddin.blog.blogappapis.Config.MetricsConfig.BusinessMetrics businessMetrics;
    private final UserProfilePhotoService userProfilePhotoService;

    public UserController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            SessionService sessionService,
            PasswordValidationService passwordValidationService,
            AuditService auditService,
            com.siyamuddin.blog.blogappapis.Config.MetricsConfig.BusinessMetrics businessMetrics,
            UserProfilePhotoService userProfilePhotoService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.passwordValidationService = passwordValidationService;
        this.auditService = auditService;
        this.businessMetrics = businessMetrics;
        this.userProfilePhotoService = userProfilePhotoService;
    }

    @Operation(
        summary = "Update user",
        description = "Update user information. Requires admin role or ownership of the user account."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        )
    })
    @PreAuthorize("@authz.canModifyUser(authentication,#userId)")
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @Valid @org.springframework.validation.annotation.Validated(ValidationGroups.Update.class) @RequestBody UserDto userDto,
            @Parameter(description = "User ID", required = true)
            @PathVariable Integer userId)
    {
        UserDto updatedUserDto=this.userService.updateUser(userDto,userId);
        // Audit user update
        try {
            User user = userService.getUserEntityById(userId);
            auditService.logUserAction(user, "USER_UPDATED", "USER", userId);
            businessMetrics.incrementUserUpdate();
        } catch (Exception e) {
            // log.warn("Could not audit user update: {}", e.getMessage());
        }
        return ResponseEntity.ok(updatedUserDto);
    }
    @Operation(
        summary = "Delete user",
        description = "Delete a user account. Requires admin role or ownership of the user account."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Access denied"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        )
    })
    @PreAuthorize("@authz.canModifyUser(authentication,#userId)")
    @DeleteMapping("/{userId}")
   public ResponseEntity<ApiResponse> deleteUser(
           @Parameter(description = "User ID", required = true)
           @PathVariable Integer userId)
   {
       // Get user before deletion for audit
       try {
           User user = userService.getUserEntityById(userId);
           this.userService.deleteUser(userId);
           // Audit user deletion
           auditService.logUserAction(user, "USER_DELETED", "USER", userId);
           businessMetrics.incrementUserDelete();
       } catch (Exception e) {
           // If user not found, still try to delete (idempotent)
           this.userService.deleteUser(userId);
       }
       return new ResponseEntity<>(new ApiResponse("User deleted successfully",true),HttpStatus.OK);
   }

    @Operation(
        summary = "Get user by ID",
        description = "Retrieve user information by user ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        )
    })
  @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Integer userId)
  {
      UserDto userDto=this.userService.getUserById(userId);
      return new ResponseEntity<>(userDto,HttpStatus.OK);
  }
    @Operation(
        summary = "Get all users",
        description = "Retrieve paginated list of all users with sorting support"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        )
    })
  @GetMapping("/")
  public ResponseEntity<PagedResponse<UserDto>> getAllUser(
          @Parameter(description = "Page number (0-indexed)", example = "0")
          @RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
          @Parameter(description = "Page size", example = "10")
          @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
          @Parameter(description = "Sort field", example = "id")
          @RequestParam(value = "sortBy",defaultValue = "id",required = false) String sortBy,
          @Parameter(description = "Sort direction (asc/desc)", example = "asc")
          @RequestParam(value = "sortDirec",defaultValue = AppConstants.SORT_DIREC,required = false) String sortDirec)
  {
      PagedResponse<UserDto> pagedResponse = this.userService.getAllUser(pageNumber, pageSize, sortBy, sortDirec);
      return new ResponseEntity<>(pagedResponse, HttpStatus.OK);
  }

    @Operation(
        summary = "Search users by name",
        description = "Search users by name containing the provided keywords"
    )
    @GetMapping("/search/{keywords}")
    public ResponseEntity<List<UserDto>> searchUserByName(
            @Parameter(description = "Search keywords", required = true)
            @PathVariable("keywords") String keywords) {
        List<UserDto> userDtos = this.userService.searchUserByName(keywords);
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }
    
    @Operation(
        summary = "Get current user",
        description = "Get information about the currently authenticated user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User information retrieved",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        )
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityByEmail(email);
        UserDto userDto = userService.getUserById(user.getId());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    
    @Operation(
        summary = "Update current user profile",
        description = "Update the currently authenticated user's profile information"
    )
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @Valid @org.springframework.validation.annotation.Validated(ValidationGroups.Update.class) @RequestBody UserDto userDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityByEmail(email);
        UserDto updatedUserDto = this.userService.updateUser(userDto, user.getId());
        // Audit profile update
        auditService.logUserAction(user, "PROFILE_UPDATED", "USER", user.getId());
        return ResponseEntity.ok(updatedUserDto);
    }
    
    @Operation(
        summary = "Change password",
        description = "Change the current user's password. Rate limited for security."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Password changed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid current password or weak new password"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429", 
            description = "Too many password change attempts"
        )
    })
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Parameter(description = "Current password", required = true)
            @RequestParam @jakarta.validation.constraints.NotBlank(message = "Current password is required") String currentPassword,
            @Parameter(description = "New password (must meet strength requirements)", required = true)
            @RequestParam @jakarta.validation.constraints.NotBlank(message = "New password is required") String newPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityByEmail(email);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return new ResponseEntity<>(
                new ApiResponse("Current password is incorrect", false),
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Validate new password strength
        try {
            passwordValidationService.validatePassword(newPassword);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Update password through service
        userService.changeUserPassword(user, newPassword);
        
        // Audit password change
        auditService.logSecurityEvent(user, "PASSWORD_CHANGED", true);
        businessMetrics.incrementPasswordChange();
        
        return new ResponseEntity<>(
            new ApiResponse("Password changed successfully", true),
            HttpStatus.OK
        );
    }
    
    @Operation(
        summary = "Get active sessions",
        description = "Get all active sessions for the current user"
    )
    @GetMapping("/me/sessions")
    public ResponseEntity<List<UserSession>> getActiveSessions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityByEmail(email);
        List<UserSession> sessions = sessionService.getActiveSessions(user.getId());
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }
    
    @Operation(
        summary = "Revoke session",
        description = "Revoke a specific user session by session ID"
    )
    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<ApiResponse> revokeSession(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String sessionId) {
        sessionService.invalidateSession(sessionId);
        return new ResponseEntity<>(
            new ApiResponse("Session revoked successfully", true),
            HttpStatus.OK
        );
    }

    @Operation(
        summary = "Upload user profile photo",
        description = "Upload or replace a user's profile photo."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile photo updated",
            content = @Content(schema = @Schema(implementation = UserDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file provided"
        )
    })
    @PreAuthorize("@authz.canModifyUser(authentication,#userId)")
    @PostMapping(
        value = "/{userId}/profile-photo",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserDto> uploadProfilePhoto(
            @Parameter(description = "User ID", required = true)
            @PathVariable Integer userId,
            @Parameter(description = "Profile photo file (jpg, png, webp, gif, avif, pdf)", required = true)
            @RequestPart("file") MultipartFile file) {

        UserDto updated = userProfilePhotoService.uploadProfilePhoto(userId, file);
        try {
            User user = userService.getUserEntityById(userId);
            auditService.logUserAction(user, "PROFILE_PHOTO_UPDATED", "USER", userId);
        } catch (Exception e) {
            // Best-effort auditing
        }
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Upload current user's profile photo",
        description = "Upload or replace the authenticated user's profile photo."
    )
    @PostMapping(
        value = "/me/profile-photo",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserDto> uploadMyProfilePhoto(
            @Parameter(description = "Profile photo file (jpg, png, webp, gif, avif, pdf)", required = true)
            @RequestPart("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityByEmail(email);
        UserDto updated = userProfilePhotoService.uploadProfilePhoto(user.getId(), file);
        auditService.logUserAction(user, "PROFILE_PHOTO_UPDATED", "USER", user.getId());
        return ResponseEntity.ok(updated);
    }
}
