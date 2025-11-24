package com.siyamuddin.blog.blogappapis.Config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private Login login = new Login();
    private Registration registration = new Registration();
    private General general = new General();
    
    @Getter
    @Setter
    public static class Login {
        private Integer requests = 10;
        private Integer duration = 1; // hours
    }
    
    @Getter
    @Setter
    public static class Registration {
        private Integer requests = 10;
        private Integer duration = 1; // hours
    }
    
    @Getter
    @Setter
    public static class General {
        private Integer requests = 50000;
        private Integer duration = 1; // hours
    }
}

