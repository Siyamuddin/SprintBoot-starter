package com.siyamuddin.blog.blogappapis.Services.Storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoredFile {
    /**
     * Storage key (relative path for local, object key for S3).
     */
    private String key;
    /**
     * Publicly accessible URL or URI.
     */
    private String publicUrl;
    private String originalFileName;
    private String contentType;
    private long size;
}

