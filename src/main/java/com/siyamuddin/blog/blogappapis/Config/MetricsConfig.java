package com.siyamuddin.blog.blogappapis.Config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for Micrometer metrics.
 * Provides custom metrics for business operations and application monitoring.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Component to register and manage custom business metrics.
     */
    @Component
    public static class BusinessMetrics {
        private final MeterRegistry meterRegistry;
        
        // Counters for business events
        private final Counter loginAttemptsCounter;
        private final Counter loginSuccessCounter;
        private final Counter loginFailureCounter;
        private final Counter registrationCounter;
        private final Counter passwordResetRequestCounter;
        private final Counter passwordResetSuccessCounter;
        private final Counter emailVerificationCounter;
        private final Counter passwordChangeCounter;
        private final Counter userUpdateCounter;
        private final Counter userDeleteCounter;
        
        // Timers for operation durations
        private final Timer loginTimer;
        private final Timer registrationTimer;
        private final Timer passwordResetTimer;
        private final Timer emailVerificationTimer;
        
        // Gauges for current state
        private final AtomicInteger activeSessions = new AtomicInteger(0);
        private final AtomicInteger lockedAccounts = new AtomicInteger(0);
        
        @Autowired
        public BusinessMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            
            // Initialize counters
            this.loginAttemptsCounter = Counter.builder("app.auth.login.attempts")
                    .description("Total number of login attempts")
                    .tag("type", "all")
                    .register(meterRegistry);
            
            this.loginSuccessCounter = Counter.builder("app.auth.login.attempts")
                    .description("Total number of successful logins")
                    .tag("type", "success")
                    .register(meterRegistry);
            
            this.loginFailureCounter = Counter.builder("app.auth.login.attempts")
                    .description("Total number of failed logins")
                    .tag("type", "failure")
                    .register(meterRegistry);
            
            this.registrationCounter = Counter.builder("app.auth.registrations")
                    .description("Total number of user registrations")
                    .register(meterRegistry);
            
            this.passwordResetRequestCounter = Counter.builder("app.auth.password.reset.requests")
                    .description("Total number of password reset requests")
                    .register(meterRegistry);
            
            this.passwordResetSuccessCounter = Counter.builder("app.auth.password.reset.success")
                    .description("Total number of successful password resets")
                    .register(meterRegistry);
            
            this.emailVerificationCounter = Counter.builder("app.auth.email.verifications")
                    .description("Total number of email verifications")
                    .register(meterRegistry);
            
            this.passwordChangeCounter = Counter.builder("app.auth.password.changes")
                    .description("Total number of password changes")
                    .register(meterRegistry);
            
            this.userUpdateCounter = Counter.builder("app.users.updates")
                    .description("Total number of user profile updates")
                    .register(meterRegistry);
            
            this.userDeleteCounter = Counter.builder("app.users.deletes")
                    .description("Total number of user deletions")
                    .register(meterRegistry);
            
            // Initialize timers
            this.loginTimer = Timer.builder("app.auth.login.duration")
                    .description("Time taken for login operations")
                    .register(meterRegistry);
            
            this.registrationTimer = Timer.builder("app.auth.registration.duration")
                    .description("Time taken for registration operations")
                    .register(meterRegistry);
            
            this.passwordResetTimer = Timer.builder("app.auth.password.reset.duration")
                    .description("Time taken for password reset operations")
                    .register(meterRegistry);
            
            this.emailVerificationTimer = Timer.builder("app.auth.email.verification.duration")
                    .description("Time taken for email verification operations")
                    .register(meterRegistry);
            
            // Register gauges
            Gauge.builder("app.sessions.active", activeSessions, AtomicInteger::get)
                    .description("Current number of active user sessions")
                    .register(meterRegistry);
            
            Gauge.builder("app.accounts.locked", lockedAccounts, AtomicInteger::get)
                    .description("Current number of locked user accounts")
                    .register(meterRegistry);
        }
        
        // Counter methods
        public void incrementLoginAttempts() {
            loginAttemptsCounter.increment();
        }
        
        public void incrementLoginSuccess() {
            loginSuccessCounter.increment();
        }
        
        public void incrementLoginFailure() {
            loginFailureCounter.increment();
        }
        
        public void incrementRegistration() {
            registrationCounter.increment();
        }
        
        public void incrementPasswordResetRequest() {
            passwordResetRequestCounter.increment();
        }
        
        public void incrementPasswordResetSuccess() {
            passwordResetSuccessCounter.increment();
        }
        
        public void incrementEmailVerification() {
            emailVerificationCounter.increment();
        }
        
        public void incrementPasswordChange() {
            passwordChangeCounter.increment();
        }
        
        public void incrementUserUpdate() {
            userUpdateCounter.increment();
        }
        
        public void incrementUserDelete() {
            userDeleteCounter.increment();
        }
        
        // Timer methods
        public Timer.Sample startLoginTimer() {
            return Timer.start(meterRegistry);
        }
        
        public void recordLoginDuration(Timer.Sample sample) {
            sample.stop(loginTimer);
        }
        
        public Timer.Sample startRegistrationTimer() {
            return Timer.start(meterRegistry);
        }
        
        public void recordRegistrationDuration(Timer.Sample sample) {
            sample.stop(registrationTimer);
        }
        
        public Timer.Sample startPasswordResetTimer() {
            return Timer.start(meterRegistry);
        }
        
        public void recordPasswordResetDuration(Timer.Sample sample) {
            sample.stop(passwordResetTimer);
        }
        
        public Timer.Sample startEmailVerificationTimer() {
            return Timer.start(meterRegistry);
        }
        
        public void recordEmailVerificationDuration(Timer.Sample sample) {
            sample.stop(emailVerificationTimer);
        }
        
        // Gauge methods
        public void incrementActiveSessions() {
            activeSessions.incrementAndGet();
        }
        
        public void decrementActiveSessions() {
            activeSessions.decrementAndGet();
        }
        
        public void setActiveSessions(int count) {
            activeSessions.set(count);
        }
        
        public void incrementLockedAccounts() {
            lockedAccounts.incrementAndGet();
        }
        
        public void decrementLockedAccounts() {
            lockedAccounts.decrementAndGet();
        }
        
        public void setLockedAccounts(int count) {
            lockedAccounts.set(count);
        }
    }
}

