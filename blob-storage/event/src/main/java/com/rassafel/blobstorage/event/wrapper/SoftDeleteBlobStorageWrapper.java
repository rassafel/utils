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
import com.rassafel.blobstorage.core.NotFoundBlobException;
import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import com.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest;
import com.rassafel.blobstorage.core.support.wrapper.DefaultDelegatedBlobStorage;
import com.rassafel.blobstorage.event.BlobEventPublisher;
import com.rassafel.blobstorage.event.type.RestoreSoftDeletedBlobEvent;
import com.rassafel.blobstorage.event.type.SoftDeleteBlobEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Soft delete blob storage wrapper.
 * <p>
 * Events:
 * <li>{@link SoftDeleteBlobEvent}</li>
 * <li>{@link RestoreSoftDeletedBlobEvent}</li>
 */
@Slf4j
public class SoftDeleteBlobStorageWrapper extends DefaultDelegatedBlobStorage {
    public static final String DEFAULT_DELETED_ATTRIBUTE = "X-Deleted";
    private final Clock clock;
    private final BlobEventPublisher publisher;
    private final String deletedAttribute;

    public SoftDeleteBlobStorageWrapper(BlobStorage delegate, Clock clock, BlobEventPublisher publisher) {
        this(delegate, clock, publisher, DEFAULT_DELETED_ATTRIBUTE);
    }

    public SoftDeleteBlobStorageWrapper(BlobStorage delegate, Clock clock, BlobEventPublisher publisher, String deletedAttribute) {
        super(delegate);
        Assert.notNull(clock, "clock cannot be null");
        Assert.notNull(publisher, "publisher cannot be null");
        Assert.hasText(deletedAttribute, "deletedAttribute cannot be null");
        this.clock = clock;
        this.publisher = publisher;
        this.deletedAttribute = deletedAttribute;
    }

    @Override
    public boolean existsByRef(String ref) {
        return findByRef(ref).isPresent();
    }

    public boolean existsByRefIgnoreDeleted(String ref) {
        return super.existsByRef(ref);
    }

    @Nullable
    @Override
    public StoredBlobObject getByRef(String ref) {
        var blob = getByRefIgnoreDeleted(ref);
        if (blob == null) return null;
        if (isDeleted(blob)) {
            log.trace("Blob soft deleted, skip, ref: {}", ref);
            return null;
        }
        return blob;
    }

    @Nullable
    public StoredBlobObject getByRefIgnoreDeleted(String ref) {
        return super.getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return findByRefIgnoreDeleted(ref)
            .filter(b -> {
                if (isDeleted(b)) {
                    log.trace("Blob soft deleted, skip, ref: {}", ref);
                    return false;
                }
                return true;
            });
    }

    public Optional<StoredBlobObject> findByRefIgnoreDeleted(String ref) {
        return super.findByRef(ref);
    }

    protected boolean isDeleted(StoredBlobObject blob) {
        return StringUtils.isNotBlank(blob.getAttribute(deletedAttribute));
    }

    @Override
    public boolean deleteByRef(String ref) {
        if (!existsByRef(ref)) return false;
        var timestamp = LocalDateTime.now(clock);
        var request = DefaultUpdateAttributesRequest.builder()
            .attribute(deletedAttribute, timestamp.toString())
            .build();
        log.trace("Add soft delete attribute to blob, ref: {}; '{}': '{}'", ref, deletedAttribute, timestamp);
        try {
            var response = updateByRefIgnoreDeleted(ref, request);
        } catch (NotFoundBlobException ex) {
            log.debug("Fail add soft delete attribute to blob, ref: {}; '{}': '{}'", ref, deletedAttribute, timestamp);
            return false;
        }
        log.debug("Soft delete attribute added to blob, ref: {}; '{}': '{}'", ref, deletedAttribute, timestamp);
        var event = new SoftDeleteBlobEvent(getDelegate(), timestamp, ref);
        log.debug("Publish SoftDeleteBlobEvent, ref: {}", ref);
        publisher.publish(event);
        return true;
    }

    /**
     * Restore soft deleted blob
     *
     * @param ref blob reference
     * @return false if blob is not soft deleted, true if soft deleted
     * @throws NotFoundBlobException if not exists
     */
    public boolean restoreDeletedByRef(String ref) {
        var blob = findByRefIgnoreDeleted(ref)
            .orElseThrow(() -> new NotFoundBlobException(ref));
        if (!isDeleted(blob)) return false;
        var timestamp = blob.getAttribute(deletedAttribute);
        var request = DefaultUpdateAttributesRequest.builder()
            .removeAttribute(deletedAttribute)
            .build();
        log.trace("Remove soft delete attribute to blob, ref: {}; '{}': '{}'", ref, deletedAttribute, timestamp);
        try {
            var response = updateByRefIgnoreDeleted(ref, request);
        } catch (NotFoundBlobException ex) {
            log.debug("Fail remove soft delete attribute from blob, ref: {}; '{}': '{}'", ref, deletedAttribute,
                timestamp);
            throw ex;
        }
        log.debug("Soft delete attribute removed to blob, ref: {}; '{}': '{}'", ref, deletedAttribute, timestamp);
        var event = new RestoreSoftDeletedBlobEvent(getDelegate(), clock, ref);
        publisher.publish(event);
        return true;
    }

    public boolean hardDeleteByRef(String ref) {
        return super.deleteByRef(ref);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        if (!existsByRef(ref)) throw new NotFoundBlobException(ref);
        return updateByRefIgnoreDeleted(ref, request);
    }

    public UpdateAttributesResponse updateByRefIgnoreDeleted(String ref, UpdateAttributesRequest request) {
        return super.updateByRef(ref, request);
    }
}
