package com.rassafel.io.storage.core.support.wrapper;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;

/**
 * Delegated BlobStorage
 */
public class DefaultDelegatedBlobStorage implements BlobStorage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final BlobStorage delegate;

    public DefaultDelegatedBlobStorage(BlobStorage delegate) {
        Assert.notNull(delegate, "delegate cannot be null");
        this.delegate = delegate;
    }

    protected BlobStorage getDelegate() {
        return delegate;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        return getDelegate().store(inputStream, request);
    }

    @Override
    public boolean existsByRef(String ref) {
        return getDelegate().existsByRef(ref);
    }

    @Override
    public StoredBlobObject getByRef(String ref) {
        return getDelegate().getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return getDelegate().findByRef(ref);
    }

    @Override
    public boolean deleteByRef(String ref) {
        return getDelegate().deleteByRef(ref);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        return getDelegate().updateByRef(ref, request);
    }
}
