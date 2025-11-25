package com.siyamuddin.blog.blogappapis.Services.Storage;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class FileUploadRequest {
    private MultipartFile file;
    /**
     * Optional sub-directory relative to the configured base path/root folder.
     */
    private String subDirectory;
    /**
     * Optional file name override (should include extension). Defaults to UUID + original extension.
     */
    private String preferredFileName;
    /**
     * Whether the upload should overwrite an existing file with the same resolved key.
     */
    @Builder.Default
    private boolean overwrite = true;
}

