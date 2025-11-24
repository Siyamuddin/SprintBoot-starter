package com.siyamuddin.blog.blogappapis.Controllers;

import com.siyamuddin.blog.blogappapis.Config.AppConstants;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Entity.UserSession;
import com.siyamuddin.blog.blogappapis.Payloads.ApiResponse;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.SessionService;
import com.siyamuddin.blog.blogappapis.Services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "JWT-Auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SessionService sessionService;

//    @PostMapping("/")
//    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto)
//    {
//        UserDto createdUserDto= this.userService.createUser(userDto);
//        return new ResponseEntity<>(createdUserDto, HttpStatus.CREATED);
//    }
@PreAuthorize("@authz.canModifyUser(authentication,#userId)")
@PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto,@PathVariable Integer userId)
    {
        UserDto updatedUserDto=this.userService.updateUser(userDto,userId);
        return ResponseEntity.ok(updatedUserDto);
    }
    @PreAuthorize("@authz.canModifyUser(authentication,#userId)")
    @DeleteMapping("/{userId}")
   public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer userId)
   {
       this.userService.deleteUser(userId);
       return new ResponseEntity<>(new ApiResponse("User deleted successfully",true),HttpStatus.OK);
   }

  @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Integer userId)
  {
      UserDto userDto=this.userService.getUserById(userId);
      return new ResponseEntity<>(userDto,HttpStatus.OK);
  }
  @GetMapping("/")
  public ResponseEntity<List<UserDto>> getAllUser(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                  @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                  @RequestParam(value = "sortBy",defaultValue = "id",required = false) String sortBy,
                                                  @RequestParam(value = "sortDirec",defaultValue = AppConstants.SORT_DIREC,required = false) String sortDirec)
  {
      List<UserDto> userDtos=this.userService.getAllUser(pageNumber,pageSize,sortBy,sortDirec);
      return new ResponseEntity<List<UserDto>>(userDtos,HttpStatus.OK);
  }

    @GetMapping("/search/{keywords}")
    public ResponseEntity<List<UserDto>> searchUserByName(@PathVariable("keywords") String keywords) {
        List<UserDto> userDtos = this.userService.searchUserByName(keywords);
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDto userDto = userService.getUserById(user.getId());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UserDto userDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDto updatedUserDto = this.userService.updateUser(userDto, user.getId());
        return ResponseEntity.ok(updatedUserDto);
    }
    
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return new ResponseEntity<>(
                new ApiResponse("Current password is incorrect", false),
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        
        return new ResponseEntity<>(
            new ApiResponse("Password changed successfully", true),
            HttpStatus.OK
        );
    }
    
    @GetMapping("/me/sessions")
    public ResponseEntity<List<UserSession>> getActiveSessions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<UserSession> sessions = sessionService.getActiveSessions(user.getId());
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }
    
    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<ApiResponse> revokeSession(@PathVariable String sessionId) {
        sessionService.invalidateSession(sessionId);
        return new ResponseEntity<>(
            new ApiResponse("Session revoked successfully", true),
            HttpStatus.OK
        );
    }
}
