package com.rassafel.io.storage.core.event.wrapper;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.event.BlobEventPublisher;
import com.rassafel.io.storage.core.event.type.HardDeleteBlobEvent;
import com.rassafel.io.storage.core.event.type.UpdateAttributesBlobEvent;
import com.rassafel.io.storage.core.event.type.UploadBlobEvent;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import lombok.val;

import java.io.InputStream;
import java.time.Clock;
import java.util.Optional;

/**
 * Blob event publishing storage wrapper.
 * <p>
 * Events:
 * <li>{@link UploadBlobEvent}</li>
 * <li>{@link UpdateAttributesBlobEvent}</li>
 * <li>{@link HardDeleteBlobEvent}</li>
 */
public class EventPublisherBlobStorageWrapper implements BlobStorage {
    private final BlobStorage delegate;
    private final Clock clock;
    private final BlobEventPublisher publisher;

    public EventPublisherBlobStorageWrapper(BlobStorage delegate, Clock clock, BlobEventPublisher publisher) {
        Assert.notNull(delegate, "delegate cannot be null");
        Assert.notNull(clock, "clock cannot be null");
        Assert.notNull(publisher, "publisher cannot be null");
        this.delegate = delegate;
        this.clock = clock;
        this.publisher = publisher;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        val response = delegate.store(inputStream, request);
        val event = new UploadBlobEvent(delegate, clock, response.getStoredObject().getStoredRef());
        publisher.publish(event);
        return response;
    }

    @Override
    public boolean existsByRef(String ref) {
        return delegate.existsByRef(ref);
    }

    @Override
    public StoredBlobObject getByRef(String ref) {
        return delegate.getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return delegate.findByRef(ref);
    }

    @Override
    public boolean deleteByRef(String ref) {
        val result = delegate.deleteByRef(ref);
        if (result) {
            val event = new HardDeleteBlobEvent(delegate, clock, ref);
            publisher.publish(event);
        }
        return result;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val response = delegate.updateByRef(ref, request);
        val event = new UpdateAttributesBlobEvent(delegate, clock, ref);
        publisher.publish(event);
        return response;
    }
}
