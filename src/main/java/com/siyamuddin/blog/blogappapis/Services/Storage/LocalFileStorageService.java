package com.siyamuddin.blog.blogappapis.Services.Storage;

import com.siyamuddin.blog.blogappapis.Config.Properties.FileStorageProperties;
import com.siyamuddin.blog.blogappapis.Exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProperties properties;

    public LocalFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredFile store(FileUploadRequest request) {
        MultipartFile file = requireFile(request);
        Path basePath = Path.of(properties.getLocal().getBasePath()).toAbsolutePath();
        Path targetDirectory = resolveTargetDirectory(basePath, request.getSubDirectory());
        createDirectories(targetDirectory);

        String fileName = resolveFileName(file, request.getPreferredFileName());
        Path destination = targetDirectory.resolve(fileName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(
                    inputStream,
                    destination,
                    request.isOverwrite() ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{});
            if (log.isDebugEnabled()) {
                log.debug("Stored file locally at {}", destination);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file locally", e);
        }

        String key = basePath.relativize(destination).toString().replace("\\", "/");
        return StoredFile.builder()
                .key(key)
                .publicUrl(buildPublicUrl(key))
                .contentType(file.getContentType())
                .originalFileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }

    @Override
    public void delete(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        Path basePath = Path.of(properties.getLocal().getBasePath()).toAbsolutePath();
        Path filePath = basePath.resolve(key).normalize();
        try {
            Files.deleteIfExists(filePath);
            if (log.isDebugEnabled()) {
                log.debug("Deleted local file {}", filePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete local file " + key, e);
        }
    }

    private MultipartFile requireFile(FileUploadRequest request) {
        if (request == null || request.getFile() == null) {
            throw new FileStorageException("Upload request must include a file");
        }
        return request.getFile();
    }

    private Path resolveTargetDirectory(Path basePath, String subDirectory) {
        if (!StringUtils.hasText(subDirectory)) {
            return basePath;
        }
        Path sanitized = Path.of(subDirectory).normalize();
        if (sanitized.startsWith("..")) {
            throw new FileStorageException("Sub-directory cannot traverse outside the base path");
        }
        return basePath.resolve(sanitized);
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new FileStorageException("Failed to create directory " + directory, e);
        }
    }

    private String resolveFileName(MultipartFile file, String preferredFileName) {
        if (StringUtils.hasText(preferredFileName)) {
            return preferredFileName;
        }
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extension;
    }

    private String buildPublicUrl(String key) {
        String prefix = properties.getLocal().getPublicUriPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/uploads";
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + key;
    }
}

