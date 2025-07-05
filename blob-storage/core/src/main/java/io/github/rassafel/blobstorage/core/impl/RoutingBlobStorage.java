/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.rassafel.blobstorage.core.impl;

import java.io.InputStream;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.BlobStorageLocator;
import io.github.rassafel.blobstorage.core.StoreBlobException;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;

/**
 * Routing BlobStorage use BlobStorageLocator.
 */
@Slf4j
public class RoutingBlobStorage implements BlobStorage {
    protected static final String DEFAULT_DELIMITER = ":/";
    private final String idDelimiter;
    @NonNull
    private final BlobStorageLocator locator;

    public RoutingBlobStorage(BlobStorageLocator locator) {
        this(locator, DEFAULT_DELIMITER);
    }

    public RoutingBlobStorage(BlobStorageLocator locator, String idDelimiter) {
        Assert.hasText(idDelimiter, "idDelimiter cannot be empty");
        this.locator = locator;
        this.idDelimiter = idDelimiter;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        var storage = getDefaultStorage();
        return store(storage, inputStream, request);
    }

    /**
     * Store blob to specific storage.
     *
     * @param storageName storage name
     * @param inputStream input stream
     * @param request     store blob request
     * @return store blob response
     */
    public StoreBlobResponse store(@Nullable String storageName, InputStream inputStream, StoreBlobRequest request) {
        var storage = getStorageByName(storageName);
        return store(storage, inputStream, request);
    }

    protected StoreBlobResponse store(BlobStorage storage, InputStream inputStream, StoreBlobRequest request) {
        return storage.store(inputStream, request);
    }

    @Override
    public boolean existsByRef(String ref) {
        var storage = getStorageByRef(ref);
        return storage.existsByRef(ref);
    }

    @Nullable
    @Override
    public StoredBlobObject getByRef(String ref) {
        var storage = getStorageByRef(ref);
        return storage.getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        var storage = getStorageByRef(ref);
        return storage.findByRef(ref);
    }

    @Override
    public boolean deleteByRef(String ref) {
        var storage = getStorageByRef(ref);
        return storage.deleteByRef(ref);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var storage = getStorageByRef(ref);
        return storage.updateByRef(ref, request);
    }

    protected BlobStorage getDefaultStorage() {
        return locator.getDefaultStorage();
    }

    protected BlobStorage getStorageByRef(String ref) {
        var storageName = extractName(ref);
        return getStorageByName(storageName);
    }

    @Nullable
    protected String extractName(String ref) {
        var i = ref.indexOf(idDelimiter);
        if (i == -1) return null;
        return ref.substring(0, i);
    }

    protected BlobStorage getStorageByName(@Nullable String storageName) {
        var storage = findStorageByName(storageName);
        if (storage == null) {
            log.warn("Storage not found: {}", storageName);
            throw new StoreBlobException("Storage not found: " + storageName);
        }
        return storage;
    }

    @Nullable
    protected BlobStorage findStorageByName(@Nullable String storageName) {
        return locator.findStorage(storageName);
    }
}
