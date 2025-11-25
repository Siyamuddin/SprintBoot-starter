package com.siyamuddin.blog.blogappapis.Config.Properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Configuration holder for file storage.
 * Supports switching between local disk and S3 via {@code filestorage.mode}.
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "filestorage")
public class FileStorageProperties {

    private StorageMode mode = StorageMode.LOCAL;
    private final Local local = new Local();
    private final S3 s3 = new S3();
    private final Cleanup cleanup = new Cleanup();

    @Getter
    @Setter
    public static class Local {
        /**
         * Base path for storing files locally. Relative paths are resolved against the working dir.
         */
        private String basePath = "uploads";

        /**
         * Public URI prefix exposed through {@link org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry}.
         */
        private String publicUriPrefix = "/uploads";
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
        private String region;
        private String accessKey;
        private String secretKey;
        /**
         * Optional override for the public URL (e.g. CDN). When empty defaults to AWS bucket URL.
         */
        private String publicBaseUrl;
        /**
         * Folder inside the bucket under which files will be stored. Helps namespacing per environment.
         */
        private String rootFolder = "uploads";
    }

    @Getter
    @Setter
    public static class Cleanup {
        /**
         * Whether old files should be deleted (applies when replacing profile photos).
         */
        private boolean enabled = true;
    }

    public enum StorageMode {
        LOCAL,
        S3
    }

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(local.getBasePath())) {
            throw new IllegalStateException("filestorage.local.base-path must not be empty");
        }
        if (!StringUtils.hasText(local.getPublicUriPrefix())) {
            throw new IllegalStateException("filestorage.local.public-uri-prefix must not be empty");
        }

        if (mode == StorageMode.S3) {
            if (!StringUtils.hasText(s3.getBucketName())) {
                throw new IllegalStateException("filestorage.s3.bucket-name is required when mode=S3");
            }
            if (!StringUtils.hasText(s3.getRegion())) {
                throw new IllegalStateException("filestorage.s3.region is required when mode=S3");
            }
            if (!StringUtils.hasText(s3.getAccessKey()) || !StringUtils.hasText(s3.getSecretKey())) {
                log.warn("S3 access/secret keys are missing. Default AWS credential chain will be used.");
            }
        }
        log.info("File storage configured to use mode {}", mode);
    }
}

