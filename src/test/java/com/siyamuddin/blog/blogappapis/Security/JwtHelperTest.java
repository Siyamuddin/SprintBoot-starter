package com.siyamuddin.blog.blogappapis.Security;

import com.siyamuddin.blog.blogappapis.Config.Properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtHelperTest {

    private JwtHelper jwtHelper;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        this.jwtHelper = new JwtHelper();
        this.jwtProperties = new JwtProperties();
        jwtProperties.setSecret("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghij");
        jwtProperties.setAccessTokenValidity(900L);
        jwtProperties.setRefreshTokenValidity(604800L);
        ReflectionTestUtils.setField(jwtHelper, "jwtProperties", jwtProperties);
    }

    @Test
    void generateTokenShouldProduceValidToken() {
        UserDetails userDetails = User.withUsername("test@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtHelper.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtHelper.validateToken(token, userDetails)).isTrue();
        assertThat(jwtHelper.getUsernameFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void refreshTokenShouldBeDifferentFromAccessToken() {
        UserDetails userDetails = User.withUsername("refresh@test.com")
                .password("password")
                .roles("USER")
                .build();

        String accessToken = jwtHelper.generateToken(userDetails);
        String refreshToken = jwtHelper.generateRefreshToken(userDetails);

        assertThat(accessToken).isNotEqualTo(refreshToken);
        assertThat(jwtHelper.getUsernameFromToken(accessToken)).isEqualTo("refresh@test.com");
    }
}

