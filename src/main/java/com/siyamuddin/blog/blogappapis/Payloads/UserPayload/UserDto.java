package com.siyamuddin.blog.blogappapis.Payloads.UserPayload;

import com.siyamuddin.blog.blogappapis.Entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private int id;
    
    @NotEmpty(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
              message = "Name is required")
    @Size(min=3, max=100, 
          groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
          message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(groups = {ValidationGroups.Create.class}, message = "Email is required")
    @Email(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
           message = "Email must be valid")
    private String email;
    
    // Password validation only for create, not for update
    @NotEmpty(groups = {ValidationGroups.Create.class}, message = "Password is required")
    @Size(min=8, max=128, 
          groups = {ValidationGroups.Create.class},
          message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
             groups = {ValidationGroups.Create.class},
             message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character")
    private String password;
    
    @Size(max=500, 
          groups = {ValidationGroups.Create.class, ValidationGroups.Update.class},
          message = "About must not exceed 500 characters")
    private String about;
    
    private Set<Role> roles = new HashSet<>();

    private String profileImageUrl;
}
