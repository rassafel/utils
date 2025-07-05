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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.BlobStorageLocator;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.support.wrapper.DefaultDelegatedBlobStorage;
import io.github.rassafel.blobstorage.core.support.wrapper.DefaultDelegationStoredBlobObject;

/**
 * BlobStorageLocator implementation.
 *
 * <p>Override created blob object ref use storage name.
 */
@Slf4j
public class DefaultBlobStorageLocator implements BlobStorageLocator {
    protected static final String DEFAULT_DELIMITER = ":/";
    @NonNull
    private final String idDelimiter;
    private final Map<String, BlobStorage> storages = new HashMap<>();
    private String defaultStorageName;
    @Setter
    @Nullable
    private BlobStorage defaultMatchedStorage = new UnmappedBlobStorage();

    public DefaultBlobStorageLocator(String defaultIdToStorage, Map<String, BlobStorage> idToStorage) {
        this(defaultIdToStorage, idToStorage, DEFAULT_DELIMITER);
    }

    public DefaultBlobStorageLocator(
            String defaultIdToStorage, @NonNull Map<String, BlobStorage> idToStorage, String idDelimiter) {
        Assert.hasText(idDelimiter, "idDelimiter cannot be empty");
        this.idDelimiter = idDelimiter;
        Assert.isTrue(!defaultIdToStorage.contains(idDelimiter), () -> "defaultIdToStorage cannot contain idDelimiter");
        defaultStorageName = defaultIdToStorage;
        Assert.notNull(idToStorage.get(defaultStorageName), "defaultStorage cannot be null");
        storages.putAll(idToStorage);
    }

    public void setDefaultStorageName(String defaultStorageName) {
        Assert.notNull(storages.get(defaultStorageName), "defaultStorageName not found");
        this.defaultStorageName = defaultStorageName;
    }

    public void addStorage(@NonNull String storageName, @NonNull BlobStorage blobStorage) {
        Assert.isTrue(!storageName.contains(idDelimiter), () -> "storageName cannot contain idDelimiter");
        storages.compute(storageName, (k, v) -> {
            Assert.isNull(v, () -> "BlobStorage already exists: " + storageName);
            return blobStorage;
        });
    }

    @Override
    @Nullable
    public BlobStorage findStorage(@Nullable String storageName) {
        if (storageName == null) {
            return getDefaultStorage();
        }
        var storage = storages.get(storageName);
        if (storage == null) {
            log.debug("Storage by name not found, use default storage, storage name: {}", storageName);
            return defaultMatchedStorage;
        }
        return new BlobStorageWrapper(storage, storageName);
    }

    @Override
    public BlobStorage getDefaultStorage() {
        var storage = storages.get(defaultStorageName);
        return new BlobStorageWrapper(storage, defaultStorageName);
    }

    static class UnmappedBlobStorage implements BlobStorage {
        @Override
        public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
            throw new UnsupportedOperationException("store is not supported");
        }

        @Override
        public boolean existsByRef(String ref) {
            throw unmappedException(ref);
        }

        @Override
        public StoredBlobObject getByRef(String ref) {
            throw unmappedException(ref);
        }

        @Override
        public boolean deleteByRef(String ref) {
            throw unmappedException(ref);
        }

        @Override
        public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
            throw unmappedException(ref);
        }

        private RuntimeException unmappedException(String ref) {
            return new IllegalArgumentException("There is no BlobStorage mapped for the ref \"" + ref + "\"");
        }
    }

    class BlobStorageWrapper extends DefaultDelegatedBlobStorage {
        private final String storageName;

        public BlobStorageWrapper(BlobStorage delegate, String storageName) {
            super(delegate);
            this.storageName = storageName;
        }

        @Override
        protected BlobStorage getDelegate() {
            return super.getDelegate();
        }

        @Override
        public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
            var response = super.store(inputStream, request);
            var object = new StoredBlobObjectWrapper(response.getStoredObject());
            return DefaultStoreBlobResponse.of(object);
        }

        @Override
        public boolean existsByRef(String ref) {
            ref = removeId(ref);
            return super.existsByRef(ref);
        }

        @Nullable
        @Override
        public StoredBlobObject getByRef(String ref) {
            ref = removeId(ref);
            var blob = super.getByRef(ref);
            if (blob == null) return null;
            return new StoredBlobObjectWrapper(blob);
        }

        @Override
        public Optional<StoredBlobObject> findByRef(String ref) {
            ref = removeId(ref);
            return super.findByRef(ref)
                    .map(StoredBlobObjectWrapper::new);
        }

        @Override
        public boolean deleteByRef(String ref) {
            ref = removeId(ref);
            return super.deleteByRef(ref);
        }

        @Override
        public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
            ref = removeId(ref);
            var response = super.updateByRef(ref, request);
            var storedObject = response.getStoredObject();
            return DefaultUpdateAttributesResponse.of(new StoredBlobObjectWrapper(storedObject));
        }

        protected String removeId(String ref) {
            var i = ref.indexOf(idDelimiter);
            if (i == -1) return ref;
            if (storageName.equals(ref.substring(0, i))) {
                return ref.substring(i + idDelimiter.length());
            }
            return ref;
        }

        class StoredBlobObjectWrapper extends DefaultDelegationStoredBlobObject {
            public StoredBlobObjectWrapper(StoredBlobObject delegate) {
                super(delegate);
            }

            @Override
            public String getStoredRef() {
                var delegate = getDelegate();
                return storageName + idDelimiter + delegate.getStoredRef();
            }
        }
    }
}
