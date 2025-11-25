package com.siyamuddin.blog.blogappapis.Services.Storage;

public interface FileStorageService {

    /**
     * Store a file using the configured backend.
     *
     * @param request upload metadata
     * @return descriptor representing the stored file
     */
    StoredFile store(FileUploadRequest request);

    /**
     * Delete the file represented by the given key/object path.
     * Deletion should be idempotent.
     *
     * @param key relative path (local) or object key (S3)
     */
    void delete(String key);
}

