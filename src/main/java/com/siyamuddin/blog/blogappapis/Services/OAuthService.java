package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Entity.User;

public interface OAuthService {
    User linkOAuthAccount(User user, String provider, String providerId);
    void unlinkOAuthAccount(User user, String provider);
    User findOrCreateUserFromOAuth(String provider, String providerId, String email, String name);
}

