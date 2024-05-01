package com.rassafel.io.storage.core.impl;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.BlobStorageLocator;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.util.Optional;

/**
 * Routing BlobStorage use BlobStorageLocator.
 */
@Slf4j
@RequiredArgsConstructor
public class RoutingBlobStorage implements BlobStorage {
    protected static final String DEFAULT_DELIMITER = ":";
    private final String idDelimiter;
    private final BlobStorageLocator locator;

    public RoutingBlobStorage(BlobStorageLocator locator) {
        this(locator, DEFAULT_DELIMITER);
    }

    public RoutingBlobStorage(BlobStorageLocator locator, String idDelimiter) {
        Assert.notNull(locator, "locator cannot be null");
        Assert.hasText(idDelimiter, "idDelimiter cannot be empty");
        this.locator = locator;
        this.idDelimiter = idDelimiter;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        val storage = getDefaultStorage();
        return store(storage, inputStream, request);
    }

    public StoreBlobResponse store(@Nullable String storageName, InputStream inputStream, StoreBlobRequest request) {
        val storage = findStorageByName(storageName);
        return store(storage, inputStream, request);
    }

    protected StoreBlobResponse store(BlobStorage storage, InputStream inputStream, StoreBlobRequest request) {
        return storage.store(inputStream, request);
    }

    @Override
    public boolean existsByRef(String ref) {
        val storage = findStorageByRef(ref);
        return storage.existsByRef(ref);
    }

    @Override
    public StoredBlobObject getByRef(String ref) {
        val storage = findStorageByRef(ref);
        return storage.getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        val storage = findStorageByRef(ref);
        return storage.findByRef(ref);
    }

    @Override
    public boolean deleteByRef(String ref) {
        val storage = findStorageByRef(ref);
        return storage.deleteByRef(ref);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val storage = findStorageByRef(ref);
        return storage.updateByRef(ref, request);
    }

    protected BlobStorage getDefaultStorage() {
        return locator.getDefaultStorage();
    }

    protected BlobStorage findStorageByRef(String ref) {
        val storageName = extractName(ref);
        return findStorageByName(storageName);
    }

    @Nullable
    protected String extractName(String ref) {
        val i = ref.indexOf(idDelimiter);
        if (i == -1) return null;
        return ref.substring(i + idDelimiter.length());
    }

    protected BlobStorage findStorageByName(@Nullable String storageName) {
        return locator.findStorage(storageName);
    }
}
