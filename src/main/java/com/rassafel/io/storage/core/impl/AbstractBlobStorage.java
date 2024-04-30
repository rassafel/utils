package com.rassafel.io.storage.core.impl;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.util.FileTypeUtils;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Abstract blob storage with default store implementation.
 */
public abstract class AbstractBlobStorage implements BlobStorage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final KeyGenerator keyGenerator;

    protected AbstractBlobStorage(KeyGenerator keyGenerator) {
        Assert.notNull(keyGenerator, "fileKeyGenerator must not be null");
        this.keyGenerator = keyGenerator;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        val originalName = request.getOriginalName();
        val blobKey = generateBlobKey(originalName);
        val builder = request.toBuilder();
        val contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            val type = FileTypeUtils.getMimeTypeFromName(originalName);
            builder.contentType(type.toString());
        }
        return store(blobKey, inputStream, builder.build());
    }

    protected String generateBlobKey(String originalName) {
        val blobKey = KeyGenerator.SEPARATOR + keyGenerator.createKey(originalName);
        val extension = FilenameUtils.getExtension(originalName);
        if (StringUtils.isNotBlank(extension)) {
            return blobKey + "." + extension;
        }
        return blobKey;
    }

    protected abstract StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request);
}
