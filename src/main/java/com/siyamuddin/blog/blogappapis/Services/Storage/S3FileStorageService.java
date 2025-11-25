package com.siyamuddin.blog.blogappapis.Services.Storage;

import com.siyamuddin.blog.blogappapis.Config.Properties.FileStorageProperties;
import com.siyamuddin.blog.blogappapis.Exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final FileStorageProperties properties;

    public S3FileStorageService(S3Client s3Client, FileStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public StoredFile store(FileUploadRequest request) {
        MultipartFile file = requireFile(request);
        String key = buildObjectKey(request, file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getS3().getBucketName())
                .key(key)
                .contentType(file.getContentType())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException | AwsServiceException | SdkClientException e) {
            throw new FileStorageException("Failed to upload file to S3", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Uploaded file to S3 bucket {} with key {}", properties.getS3().getBucketName(), key);
        }

        return StoredFile.builder()
                .key(key)
                .publicUrl(buildPublicUrl(key))
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    @Override
    public void delete(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getS3().getBucketName())
                    .key(key)
                    .build());
            if (log.isDebugEnabled()) {
                log.debug("Deleted S3 object {}", key);
            }
        } catch (AwsServiceException | SdkClientException e) {
            throw new FileStorageException("Failed to delete S3 object " + key, e);
        }
    }

    private MultipartFile requireFile(FileUploadRequest request) {
        if (request == null || request.getFile() == null) {
            throw new FileStorageException("Upload request must include a file");
        }
        return request.getFile();
    }

    private String buildObjectKey(FileUploadRequest request, MultipartFile file) {
        String fileName = resolveFileName(file, request.getPreferredFileName());
        StringBuilder keyBuilder = new StringBuilder();
        String rootFolder = properties.getS3().getRootFolder();
        if (StringUtils.hasText(rootFolder)) {
            keyBuilder.append(trimSlashes(rootFolder));
        }
        if (StringUtils.hasText(request.getSubDirectory())) {
            if (keyBuilder.length() > 0) {
                keyBuilder.append('/');
            }
            keyBuilder.append(trimSlashes(request.getSubDirectory()));
        }
        if (keyBuilder.length() > 0) {
            keyBuilder.append('/');
        }
        keyBuilder.append(fileName);
        return keyBuilder.toString();
    }

    private String resolveFileName(MultipartFile file, String preferredFileName) {
        if (StringUtils.hasText(preferredFileName)) {
            return trimSlashes(preferredFileName);
        }
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extension;
    }

    private String trimSlashes(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String buildPublicUrl(String key) {
        String customBaseUrl = properties.getS3().getPublicBaseUrl();
        if (StringUtils.hasText(customBaseUrl)) {
            if (!customBaseUrl.endsWith("/")) {
                customBaseUrl = customBaseUrl + "/";
            }
            return customBaseUrl + key;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                properties.getS3().getBucketName(),
                properties.getS3().getRegion(),
                key);
    }
}

