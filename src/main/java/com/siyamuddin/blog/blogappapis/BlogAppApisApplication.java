package com.siyamuddin.blog.blogappapis;

import com.siyamuddin.blog.blogappapis.Config.AppConstants;
import com.siyamuddin.blog.blogappapis.Entity.Role;
import com.siyamuddin.blog.blogappapis.Repository.RoleRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class BlogAppApisApplication implements CommandLineRunner {
	@Autowired
	private RoleRepo roleRepo;
	public static void main(String[] args) {
		SpringApplication.run(BlogAppApisApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper()
	{
		return new ModelMapper();
	}

	@Override
	public void run(String... args) throws Exception {
try{
	Role role=new Role();
	role.setId(AppConstants.ADMIN_USER);
	role.setName("ROLE_ADMIN");

	Role role1=new Role();
	role1.setId(AppConstants.NORMAL_USER);
	role1.setName("ROLE_NORMAL");

	List<Role> roles=List.of(role,role1);
	this.roleRepo.saveAll(roles);

}catch (Exception e){
	e.printStackTrace();
}
	}
}
