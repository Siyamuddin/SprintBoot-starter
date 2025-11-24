package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface UserService {
    UserDto registerNewUser(UserDto userDto);
    UserDto updateUser(UserDto user, Integer userId);
    UserDto getUserById(Integer userId);
    List<UserDto> getAllUser(Integer pageNumber, Integer pageSize, String sortBy, String sortDirec);
    void deleteUser(Integer userId);
    List<UserDto> searchUserByName(String name);
}
