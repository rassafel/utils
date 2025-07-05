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

package io.github.rassafel.blobstorage.event.wrapper;

import java.io.InputStream;
import java.time.Clock;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.support.wrapper.DefaultDelegatedBlobStorage;
import io.github.rassafel.blobstorage.event.BlobEventPublisher;
import io.github.rassafel.blobstorage.event.type.HardDeleteBlobEvent;
import io.github.rassafel.blobstorage.event.type.UpdateAttributesBlobEvent;
import io.github.rassafel.blobstorage.event.type.UploadBlobEvent;

/**
 * Blob event publishing storage wrapper.
 * <p>
 * Events:
 * <ul>
 * <li>{@link UploadBlobEvent}</li>
 * <li>{@link UpdateAttributesBlobEvent}</li>
 * <li>{@link HardDeleteBlobEvent}</li>
 * </ul>
 */
@Slf4j
public class EventPublisherBlobStorageWrapper extends DefaultDelegatedBlobStorage {
    @NonNull
    private final Clock clock;
    @NonNull
    private final BlobEventPublisher publisher;

    public EventPublisherBlobStorageWrapper(BlobStorage delegate, Clock clock, BlobEventPublisher publisher) {
        super(delegate);
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
