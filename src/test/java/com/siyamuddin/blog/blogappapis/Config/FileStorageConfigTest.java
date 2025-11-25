package com.siyamuddin.blog.blogappapis.Config;

import com.siyamuddin.blog.blogappapis.Config.Properties.FileStorageProperties;
import com.siyamuddin.blog.blogappapis.Services.Storage.FileStorageService;
import com.siyamuddin.blog.blogappapis.Services.Storage.LocalFileStorageService;
import com.siyamuddin.blog.blogappapis.Services.Storage.S3FileStorageService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageConfigTest {

    private final FileStorageConfig config = new FileStorageConfig();

    @Test
    void fileStorageService_withLocalMode_returnsLocalImplementation() {
        FileStorageProperties props = new FileStorageProperties();
        props.setMode(FileStorageProperties.StorageMode.LOCAL);
        props.getLocal().setBasePath("uploads-test");

        FileStorageService service = config.fileStorageService(props);

        assertThat(service).isInstanceOf(LocalFileStorageService.class);
    }

    @Test
    void fileStorageService_withS3Mode_returnsS3Implementation() {
        FileStorageProperties props = new FileStorageProperties();
        props.setMode(FileStorageProperties.StorageMode.S3);
        props.getS3().setBucketName("test-bucket");
        props.getS3().setRegion("us-east-1");
        props.getS3().setAccessKey("dummy");
        props.getS3().setSecretKey("dummy-secret");

        FileStorageService service = config.fileStorageService(props);

        assertThat(service).isInstanceOf(S3FileStorageService.class);
    }
}

