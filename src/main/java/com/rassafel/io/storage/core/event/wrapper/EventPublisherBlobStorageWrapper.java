package com.rassafel.io.storage.core.event.wrapper;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.event.BlobEventPublisher;
import com.rassafel.io.storage.core.event.type.HardDeleteBlobEvent;
import com.rassafel.io.storage.core.event.type.UpdateAttributesBlobEvent;
import com.rassafel.io.storage.core.event.type.UploadBlobEvent;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.support.wrapper.DefaultDelegatedBlobStorage;
import lombok.val;

import java.io.InputStream;
import java.time.Clock;

/**
 * Blob event publishing storage wrapper.
 * <p>
 * Events:
 * <li>{@link UploadBlobEvent}</li>
 * <li>{@link UpdateAttributesBlobEvent}</li>
 * <li>{@link HardDeleteBlobEvent}</li>
 */
public class EventPublisherBlobStorageWrapper extends DefaultDelegatedBlobStorage {
    private final Clock clock;
    private final BlobEventPublisher publisher;

    public EventPublisherBlobStorageWrapper(BlobStorage delegate, Clock clock, BlobEventPublisher publisher) {
        super(delegate);
        Assert.notNull(clock, "clock cannot be null");
        Assert.notNull(publisher, "publisher cannot be null");
        this.clock = clock;
        this.publisher = publisher;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        val response = super.store(inputStream, request);
        val ref = response.getStoredObject().getStoredRef();
        val event = new UploadBlobEvent(getDelegate(), clock, ref);
        log.debug("Publish UploadBlobEvent, ref: {}", ref);
        publisher.publish(event);
        return response;
    }

    @Override
    public boolean deleteByRef(String ref) {
        val result = super.deleteByRef(ref);
        if (result) {
            val event = new HardDeleteBlobEvent(getDelegate(), clock, ref);
            log.debug("Publish HardDeleteBlobEvent, ref: {}", ref);
            publisher.publish(event);
        }
        return result;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val response = super.updateByRef(ref, request);
        val event = new UpdateAttributesBlobEvent(getDelegate(), clock, ref);
        log.debug("Publish UpdateAttributesBlobEvent, ref: {}", ref);
        publisher.publish(event);
        return response;
    }
}
