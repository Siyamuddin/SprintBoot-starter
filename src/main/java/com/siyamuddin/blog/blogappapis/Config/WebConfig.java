package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.FileStorageProperties;
import com.siyamuddin.blog.blogappapis.Security.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final FileStorageProperties fileStorageProperties;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor,
                     FileStorageProperties fileStorageProperties) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (fileStorageProperties.getMode() == FileStorageProperties.StorageMode.LOCAL) {
            String handlerPattern = ensureTrailingSlash(fileStorageProperties.getLocal().getPublicUriPrefix()) + "**";
            String location = pathToLocation(fileStorageProperties.getLocal().getBasePath());
            registry.addResourceHandler(handlerPattern)
                    .addResourceLocations(location);
        }
    }

    private String pathToLocation(String basePath) {
        Path path = Path.of(basePath).toAbsolutePath();
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    private String ensureTrailingSlash(String prefix) {
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
} 