package com.rassafel.io.storage.core.impl;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.BlobStorageLocator;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Delegating blob storage.
 * Override created blob object ref use storage name.
 */
@Slf4j
public class DelegatingBlobStorage implements BlobStorage, BlobStorageLocator {
    protected static final String DEFAULT_DELIMITER = ":";
    private final String idDelimiter = DEFAULT_DELIMITER;
    private final BlobStorage defaultStorage;
    private final String defaultStorageName;
    private final Map<String, BlobStorage> storages;
    private BlobStorage defaultMatchedStorage = new UnmappedBlobStorage();

    public DelegatingBlobStorage(String defaultIdToStorage, Map<String, BlobStorage> idToStorage) {
        this(defaultIdToStorage, idToStorage, DEFAULT_DELIMITER);
    }

    public DelegatingBlobStorage(String defaultIdToStorage, Map<String, BlobStorage> idToStorage, String idDelimiter) {
        Assert.hasText(idDelimiter, "idDelimiter cannot be empty");
        Assert.notNull(defaultIdToStorage, "defaultIdToStorage cannot be null");
        Assert.isFalse(defaultIdToStorage.contains(idDelimiter), () -> "defaultIdToStorage cannot contain idDelimiter");
        defaultStorageName = defaultIdToStorage;
        Assert.notNull(idToStorage, "idToStorage cannot be null");
        defaultStorage = idToStorage.get(defaultStorageName);
        Assert.notNull(defaultStorage, "defaultStorage cannot be null");
        storages = new HashMap<>(idToStorage.size());
        idToStorage.forEach(this::addStorage);
    }

    public void setDefaultMatchedStorage(BlobStorage defaultMatchedStorage) {
        Assert.notNull(defaultMatchedStorage, "defaultMatchedFileStorage cannot be null");
        this.defaultMatchedStorage = defaultMatchedStorage;
    }

    public void addStorage(String storageName, BlobStorage blobStorage) {
        Assert.notNull(storageName, "storageName cannot be null");
        Assert.notNull(blobStorage, "blobStorage cannot be null");
        Assert.isFalse(storageName.contains(idDelimiter), () -> "storageName cannot contain idDelimiter");
        storages.compute(storageName, (k, v) -> {
            Assert.isNull(v, () -> "BlobStorage already exists: " + storageName);
            return blobStorage;
        });
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        return store(defaultStorageName, getDefaultStorage(), inputStream, request);
    }

    public StoreBlobResponse store(@Nullable String storageName, InputStream inputStream, StoreBlobRequest request) {
        BlobStorage storage = findStorage(storageName);
        if (storageName == null) {
            storageName = defaultStorageName;
        }
        return store(storageName, storage, inputStream, request);
    }

    protected StoreBlobResponse store(String storageName, BlobStorage storage, InputStream inputStream, StoreBlobRequest request) {
        val response = storage.store(inputStream, request);
        val object = new StoredBlobObjectWrapper(response.getStoredObject(), storageName);
        return new DefaultStoreBlobResponse(object);
    }

    @Override
    public boolean existsByRef(String ref) {
        val idAndRef = extractIdAndRef(ref);
        val storage = findStorage(idAndRef[0]);
        return storage.existsByRef(idAndRef[1]);
    }

    @Override
    public StoredBlobObject getByRef(String ref) {
        val idAndRef = extractIdAndRef(ref);
        val storage = findStorage(idAndRef[0]);
        val result = storage.getByRef(idAndRef[1]);
        if (result == null) return null;
        return new StoredBlobObjectWrapper(result, idAndRef[0]);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        val idAndRef = extractIdAndRef(ref);
        val storage = findStorage(idAndRef[0]);
        return storage.findByRef(idAndRef[1]).map(r ->
            new StoredBlobObjectWrapper(r, idAndRef[0]));
    }

    @Override
    public boolean deleteByRef(String ref) {
        val idAndRef = extractIdAndRef(ref);
        val storage = findStorage(idAndRef[0]);
        return storage.deleteByRef(idAndRef[1]);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val idAndRef = extractIdAndRef(ref);
        val storage = findStorage(idAndRef[0]);
        val response = storage.updateByRef(idAndRef[1], request);
        val storedObject = response.getStoredObject();
        return new DefaultUpdateAttributesResponse(new StoredBlobObjectWrapper(storedObject, idAndRef[0]));
    }

    protected String[] extractIdAndRef(String ref) {
        val i = ref.indexOf(idDelimiter);
        if (i == -1) return new String[]{defaultStorageName, ref};
        return new String[]{ref.substring(0, i), ref.substring(i + 1)};
    }

    @Override
    public BlobStorage findStorage(@Nullable String storageName) {
        if (storageName == null) {
            return getDefaultStorage();
        }
        val result = storages.get(storageName);
        if (result == null) {
            log.debug("Storage by name not found, use default storage, storage name: {}", storageName);
            return defaultMatchedStorage;
        }
        return result;
    }

    @Override
    public BlobStorage getDefaultStorage() {
        return defaultStorage;
    }

    private static class UnmappedBlobStorage implements BlobStorage {
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

    @AllArgsConstructor
    private static class StoredBlobObjectWrapper implements StoredBlobObject {
        private StoredBlobObject delegate;
        private String storageName;

        @Override
        public String getOriginalName() {
            return delegate.getOriginalName();
        }

        @Override
        public String getStoredRef() {
            if (StringUtils.isNotBlank(storageName)) {
                return storageName + ":" + delegate.getStoredRef();
            }
            return delegate.getStoredRef();
        }

        @Override
        public String getContentType() {
            return delegate.getContentType();
        }

        @Override
        public LocalDateTime getUploadedAt() {
            return delegate.getUploadedAt();
        }

        @Override
        public LocalDateTime getLastModifiedAt() {
            return delegate.getLastModifiedAt();
        }

        @Override
        public long getSize() {
            return delegate.getSize();
        }

        @Override
        public Map<String, String> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public String getAttribute(String key) {
            return delegate.getAttribute(key);
        }

        @Override
        public Object getImplementation() {
            return delegate.getImplementation();
        }

        @Override
        public InputStream toInputStream() throws IOException {
            return delegate.toInputStream();
        }
    }
}
