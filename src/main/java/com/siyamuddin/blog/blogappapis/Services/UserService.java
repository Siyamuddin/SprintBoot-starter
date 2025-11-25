package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Payloads.PagedResponse;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface UserService {
    UserDto registerNewUser(UserDto userDto);
    UserDto updateUser(UserDto user, Integer userId);
    UserDto getUserById(Integer userId);
    PagedResponse<UserDto> getAllUser(Integer pageNumber, Integer pageSize, String sortBy, String sortDirec);
    void deleteUser(Integer userId);
    List<UserDto> searchUserByName(String name);
    // Internal method to get User entity for audit/logging purposes
    com.siyamuddin.blog.blogappapis.Entity.User getUserEntityById(Integer userId);
    com.siyamuddin.blog.blogappapis.Entity.User getUserEntityByEmail(String email);
    void changeUserPassword(com.siyamuddin.blog.blogappapis.Entity.User user, String newPassword);
    void updateUserLastLogin(com.siyamuddin.blog.blogappapis.Entity.User user);
}
