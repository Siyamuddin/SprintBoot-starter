package com.siyamuddin.blog.blogappapis.Utils;

import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Exceptions.ResourceNotFoundException;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;

import java.util.Optional;

/**
 * Utility class for user-related operations
 */
public class UserUtils {
    
    /**
     * Finds a user by email or throws ResourceNotFoundException
     * 
     * @param userRepo User repository
     * @param email User email
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    public static User findUserByEmailOrThrow(UserRepo userRepo, String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    /**
     * Finds a user by email, returns Optional
     * 
     * @param userRepo User repository
     * @param email User email
     * @return Optional User entity
     */
    public static Optional<User> findUserByEmail(UserRepo userRepo, String email) {
        return userRepo.findByEmail(email);
    }
}

