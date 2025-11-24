package com.siyamuddin.blog.blogappapis.Services;

import com.siyamuddin.blog.blogappapis.Config.Properties.SecurityProperties;
import com.siyamuddin.blog.blogappapis.Entity.User;
import com.siyamuddin.blog.blogappapis.Repository.UserRepo;
import com.siyamuddin.blog.blogappapis.Services.Impl.AccountSecurityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSecurityServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountSecurityServiceImpl accountSecurityService;

    private SecurityProperties securityProperties;

    private User testUser;

    @BeforeEach
    void setup() {
        securityProperties = new SecurityProperties();
        securityProperties.setMaxFailedLoginAttempts(2);
        securityProperties.setAccountLockoutDurationMinutes(30);
        ReflectionTestUtils.setField(accountSecurityService, "securityProperties", securityProperties);

        testUser = new User();
        testUser.setEmail("lock@test.com");
        testUser.setName("Lock User");
        testUser.setFailedLoginAttempts(0);
    }

    @Test
    void incrementFailedLoginAttemptsShouldLockAccountAfterThreshold() {
        when(userRepo.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountSecurityService.incrementFailedLoginAttempts(testUser.getEmail());
        accountSecurityService.incrementFailedLoginAttempts(testUser.getEmail());

        verify(userRepo, atLeastOnce()).save(testUser);
        assertThat(testUser.getAccountLockedUntil()).isNotNull();
        verify(emailService, times(1))
                .sendAccountLockedEmail(eq(testUser.getEmail()), anyString(), eq(30));
    }

    @Test
    void resetFailedLoginAttemptsShouldSetCounterToZero() {
        testUser.setFailedLoginAttempts(5);
        when(userRepo.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        accountSecurityService.resetFailedLoginAttempts(testUser.getEmail());

        assertThat(testUser.getFailedLoginAttempts()).isZero();
        verify(userRepo).save(testUser);
    }
}

