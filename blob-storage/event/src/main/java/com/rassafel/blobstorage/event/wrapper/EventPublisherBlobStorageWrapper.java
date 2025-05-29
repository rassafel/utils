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

package com.rassafel.blobstorage.event.wrapper;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.query.StoreBlobResponse;
import com.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import com.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import com.rassafel.blobstorage.core.support.wrapper.DefaultDelegatedBlobStorage;
import com.rassafel.blobstorage.event.BlobEventPublisher;
import com.rassafel.blobstorage.event.type.HardDeleteBlobEvent;
import com.rassafel.blobstorage.event.type.UpdateAttributesBlobEvent;
import com.rassafel.blobstorage.event.type.UploadBlobEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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
@Slf4j
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
        var response = super.store(inputStream, request);
        var ref = response.getStoredObject().getStoredRef();
        var event = new UploadBlobEvent(getDelegate(), clock, ref);
        log.debug("Publish UploadBlobEvent, ref: {}", ref);
        publisher.publish(event);
        return response;
    }

    @Override
    public boolean deleteByRef(String ref) {
        var result = super.deleteByRef(ref);
        if (result) {
            var event = new HardDeleteBlobEvent(getDelegate(), clock, ref);
            log.debug("Publish HardDeleteBlobEvent, ref: {}", ref);
            publisher.publish(event);
        }
        return result;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var response = super.updateByRef(ref, request);
        var event = new UpdateAttributesBlobEvent(getDelegate(), clock, ref);
        log.debug("Publish UpdateAttributesBlobEvent, ref: {}", ref);
        publisher.publish(event);
        return response;
    }
}
