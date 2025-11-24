package com.siyamuddin.blog.blogappapis.Exceptions;

public class UserAlreadyExists extends RuntimeException {

    public UserAlreadyExists(String username, String id) {
        super(String.format("User %s with user Email:%s already exists with this email.", username, id));
    }
}
