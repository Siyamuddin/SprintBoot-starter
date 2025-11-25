package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.FileStorageProperties;
import com.siyamuddin.blog.blogappapis.Services.Storage.FileStorageService;
import com.siyamuddin.blog.blogappapis.Services.Storage.LocalFileStorageService;
import com.siyamuddin.blog.blogappapis.Services.Storage.S3FileStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class FileStorageConfig {

    @Bean
    public FileStorageService fileStorageService(FileStorageProperties properties) {
        return switch (properties.getMode()) {
            case S3 -> new S3FileStorageService(buildS3Client(properties), properties);
            case LOCAL -> new LocalFileStorageService(properties);
        };
    }

    private S3Client buildS3Client(FileStorageProperties properties) {
        FileStorageProperties.S3 s3 = properties.getS3();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3.getRegion()));

        AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider(s3);
        builder.credentialsProvider(credentialsProvider);

        return builder.build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider(FileStorageProperties.S3 s3) {
        if (StringUtils.hasText(s3.getAccessKey()) && StringUtils.hasText(s3.getSecretKey())) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}

