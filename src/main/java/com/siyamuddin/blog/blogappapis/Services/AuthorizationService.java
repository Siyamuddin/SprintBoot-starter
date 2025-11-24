package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("authz")
public class AuthorizationService {

    @Autowired
    private UserRepo userRepo;

    // Check if user can modify/delete another user profile
    public boolean canModifyUser(Authentication authentication, Integer userId) {
        if (authentication == null) return false;

        String currentEmail = authentication.getName();
        User currentUser = userRepo.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) return false;

        // Check if user is admin
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        // Check if user is modifying their own profile
        boolean isSelf = currentUser.getId() == userId;

        return isAdmin || isSelf;
    }
}