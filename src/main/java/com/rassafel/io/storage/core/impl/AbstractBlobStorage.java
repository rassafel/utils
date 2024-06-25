package com.rassafel.io.storage.core.impl;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.util.FileTypeUtils;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract blob storage with default store implementation.
 */
public abstract class AbstractBlobStorage implements BlobStorage {
    protected static final String CORE_DATA_PREFIX = "X-Blob-Storage-";
    protected static final String METADATA_PREFIX = CORE_DATA_PREFIX + "Meta-";
    protected static final String ORIGINAL_NAME_ATTRIBUTE = "Original-Name";
    protected static final String UPLOADED_AT_ATTRIBUTE = "Uploaded-At";
    protected static final String LAST_MODIFIED_AT_ATTRIBUTE = "Last-Modified-At";
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final KeyGenerator keyGenerator;
    protected final String coreDataPrefix;
    protected final String metadataPrefix;

    protected AbstractBlobStorage(KeyGenerator keyGenerator) {
        this(keyGenerator, CORE_DATA_PREFIX, METADATA_PREFIX);
    }

    protected AbstractBlobStorage(KeyGenerator keyGenerator, String coreDataPrefix, String metadataPrefix) {
        Assert.notNull(keyGenerator, "keyGenerator must not be null");
        Assert.notNull(coreDataPrefix, "coreDataPrefix must not be null");
        Assert.notNull(metadataPrefix, "metadataPrefix must not be null");
        this.keyGenerator = keyGenerator;
        this.coreDataPrefix = coreDataPrefix;
        this.metadataPrefix = metadataPrefix;
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


    protected Map<String, String> objectToMetadata(StoredBlobObject object) {
        val coreDataPrefix = this.coreDataPrefix;
        val attributes = object.getAttributes();
        val result = new LinkedCaseInsensitiveMap<String>(attributes.size() + 3);
        result.put(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE, object.getOriginalName());
        result.put(coreDataPrefix + UPLOADED_AT_ATTRIBUTE, object.getUploadedAt().toString());
        result.put(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE, object.getLastModifiedAt().toString());
        result.putAll(toStoreMetadata(attributes));
        return result;
    }

    protected Map<String, String> toStoreMetadata(Map<String, String> source) {
        val metadataPrefix = this.metadataPrefix;
        return source.entrySet().stream()
            .filter(e -> StringUtils.isNotBlank(e.getValue()))
            .collect(Collectors.toMap(e -> metadataPrefix + e.getKey(), Map.Entry::getValue));
    }
}
