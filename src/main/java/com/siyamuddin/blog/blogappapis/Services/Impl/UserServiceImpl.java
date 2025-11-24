package com.siyamuddin.blog.blogappapis.Services.Impl;

import com.siyamuddin.blog.blogappapis.Config.AppConstants;
import com.siyamuddin.blog.blogappapis.Entity.Role;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Exceptions.ResourceNotFoundException;
import com.siyamuddin.blog.blogappapis.Exceptions.UserAlreadyExists;
import com.siyamuddin.blog.blogappapis.Payloads.UserPayload.UserDto;
import com.siyamuddin.blog.blogappapis.Repository.RoleRepo;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    @Override
    public UserDto registerNewUser(UserDto userDto) {
        Optional<User> userCheck=userRepo.findByEmail(userDto.getEmail());
        if(userCheck.isPresent()){
            log.info("Duplicate user tried to login.");
            throw new UserAlreadyExists(userCheck.get().getName(),userDto.getEmail());
        }
        else if(userRepo.count()==0){
            User user=this.modelMapper.map(userDto,User.class);
            //encoded password
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
            //roles
            Role role= this.roleRepo.findById(AppConstants.ADMIN_USER).get();
            user.getRoles().add(role);
            User newUser=this.userRepo.save(user);
            return this.modelMapper.map(newUser,UserDto.class);
        }
        else {
        User user=this.modelMapper.map(userDto,User.class);
        //encoded password
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        //roles
        Role role= this.roleRepo.findById(AppConstants.NORMAL_USER).get();
        user.getRoles().add(role);
        User newUser=this.userRepo.save(user);
        return this.modelMapper.map(newUser,UserDto.class);}
    }

    @Override
    @CacheEvict(key = "#userId")
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user=this.userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","ID",userId));

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());
        user.setAbout(userDto.getAbout());
        User user1=this.userRepo.save(user);
        UserDto userDto1=this.modelMapper.map(user1, UserDto.class);
        return userDto1;
    }

    @Override
    @Cacheable(key = "#userId")
    public UserDto getUserById(Integer userId) {
        User user=userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","ID",userId));
        log.info(user.getRoles().toString());
        UserDto userDto=modelMapper.map(user, UserDto.class);

        return userDto;
    }

    @Override
    public List<UserDto> getAllUser(Integer pageNumber, Integer pageSize,String sortBy,String sortDirec) {
        Sort sort=null;
        if(sortDirec.equalsIgnoreCase("asc"))
        {
            sort=Sort.by(sortBy).ascending();
        }
        else
        {
            sort=Sort.by(sortBy).descending();
        }
        Pageable pageable= PageRequest.of(pageNumber,pageSize,sort);
        Page<User> users=this.userRepo.findAll(pageable);

        List<UserDto> userDtos=users.stream().map((user)->this.modelMapper.map(user,UserDto.class)).collect(Collectors.toList());

        return userDtos;
    }

    @Override
    @CacheEvict(key = "#userId")
    public void deleteUser(Integer userId) {
        userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User","ID",userId));
        userRepo.deleteById(userId);

    }

    @Override
    public List<UserDto> searchUserByName(String name) {
        List<User> users=this.userRepo.findByNameContaining(name);
        List<UserDto> userDtos=users.stream().map((user)->modelMapper.map(user,UserDto.class)).collect(Collectors.toList());
        return userDtos;
    }

}
