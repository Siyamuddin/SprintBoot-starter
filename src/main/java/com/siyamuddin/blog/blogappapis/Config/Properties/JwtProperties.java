package com.siyamuddin.blog.blogappapis.Config.Properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
    private Long accessTokenValidity = 15 * 60L; // 15 minutes in seconds
    private Long refreshTokenValidity = 7 * 24 * 60 * 60L; // 7 days in seconds
}

