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
import com.rassafel.io.storage.core.support.wrapper.DefaultDelegatedBlobStorage;
import com.rassafel.io.storage.core.support.wrapper.DefaultDelegationStoredBlobObject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BlobStorageLocator implementation.
 * <p>
 * Override created blob object ref use storage name.
 */
@Slf4j
public class DefaultBlobStorageLocator implements BlobStorageLocator {
    protected static final String DEFAULT_DELIMITER = ":/";
    private final String idDelimiter;
    private String defaultStorageName;
    private final Map<String, BlobStorage> storages;
    private BlobStorage defaultMatchedStorage = new UnmappedBlobStorage();

    public DefaultBlobStorageLocator(String defaultIdToStorage, Map<String, BlobStorage> idToStorage) {
        this(defaultIdToStorage, idToStorage, DEFAULT_DELIMITER);
    }

    public DefaultBlobStorageLocator(String defaultIdToStorage, Map<String, BlobStorage> idToStorage, String idDelimiter) {
        Assert.hasText(idDelimiter, "idDelimiter cannot be empty");
        this.idDelimiter = idDelimiter;
        Assert.notNull(defaultIdToStorage, "defaultIdToStorage cannot be null");
        Assert.isFalse(defaultIdToStorage.contains(idDelimiter), () -> "defaultIdToStorage cannot contain idDelimiter");
        defaultStorageName = defaultIdToStorage;
        Assert.notNull(idToStorage, "idToStorage cannot be null");
        Assert.notNull(idToStorage.get(defaultStorageName), "defaultStorage cannot be null");
        storages = new HashMap<>(idToStorage.size());
        idToStorage.forEach(this::addStorage);
    }

    @Nullable
    public void setDefaultMatchedStorage(BlobStorage defaultMatchedStorage) {
        this.defaultMatchedStorage = defaultMatchedStorage;
    }

    public void setDefaultStorageName(String defaultStorageName) {
        Assert.notNull(storages.get(defaultStorageName), "defaultStorageName not found");
        this.defaultStorageName = defaultStorageName;
    }

    public void addStorage(String storageName, BlobStorage blobStorage) {
        Assert.notNull(storageName, "storageName cannot be null");
        Assert.isFalse(storageName.contains(idDelimiter), () -> "storageName cannot contain idDelimiter");
        Assert.notNull(blobStorage, "blobStorage cannot be null");
        storages.compute(storageName, (k, v) -> {
            Assert.isNull(v, () -> "BlobStorage already exists: " + storageName);
            return blobStorage;
        });
    }

    @Override
    public BlobStorage findStorage(@Nullable String storageName) {
        if (storageName == null) {
            return getDefaultStorage();
        }
        val storage = storages.get(storageName);
        if (storage == null) {
            log.debug("Storage by name not found, use default storage, storage name: {}", storageName);
            return defaultMatchedStorage;
        }
        return new BlobStorageWrapper(storage, storageName);
    }

    @Override
    public BlobStorage getDefaultStorage() {
        val storage = storages.get(defaultStorageName);
        return new BlobStorageWrapper(storage, defaultStorageName);
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
            val response = super.store(inputStream, request);
            val object = new StoredBlobObjectWrapper(response.getStoredObject());
            return new DefaultStoreBlobResponse(object);
        }

        @Override
        public boolean existsByRef(String ref) {
            ref = removeId(ref);
            return super.existsByRef(ref);
        }

        @Override
        public StoredBlobObject getByRef(String ref) {
            ref = removeId(ref);
            val blob = super.getByRef(ref);
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
            val response = super.updateByRef(ref, request);
            val storedObject = response.getStoredObject();
            return new DefaultUpdateAttributesResponse(new StoredBlobObjectWrapper(storedObject));
        }

        protected String removeId(String ref) {
            val i = ref.indexOf(idDelimiter);
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
                val delegate = getDelegate();
                return storageName + idDelimiter + delegate.getStoredRef();
            }
        }
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
}
