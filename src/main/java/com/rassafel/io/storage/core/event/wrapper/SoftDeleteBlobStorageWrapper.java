package com.rassafel.io.storage.core.event.wrapper;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.NotFoundBlobException;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.event.BlobEventPublisher;
import com.rassafel.io.storage.core.event.type.RestoreSoftDeletedBlobEvent;
import com.rassafel.io.storage.core.event.type.SoftDeleteBlobEvent;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesRequest;
import com.rassafel.io.storage.core.support.wrapper.DefaultDelegatedBlobStorage;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Soft delete blob storage wrapper.
 * <p>
 * Events:
 * <li>{@link SoftDeleteBlobEvent}</li>
 * <li>{@link RestoreSoftDeletedBlobEvent}</li>
 */
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

    @Override
    public StoredBlobObject getByRef(String ref) {
        val blob = getByRefIgnoreDeleted(ref);
        if (blob == null || isDeleted(blob)) return null;
        return blob;
    }

    @Nullable
    public StoredBlobObject getByRefIgnoreDeleted(String ref) {
        return super.getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return findByRefIgnoreDeleted(ref)
            .filter(Predicate.not(this::isDeleted));
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
        val timestamp = LocalDateTime.now(clock);
        val request = DefaultUpdateAttributesRequest.builder()
            .attribute(deletedAttribute, timestamp.toString())
            .build();
        val response = updateByRefIgnoreDeleted(ref, request);
        val event = new SoftDeleteBlobEvent(getDelegate(), timestamp, ref);
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
        val blob = findByRefIgnoreDeleted(ref)
            .orElseThrow(() -> new NotFoundBlobException(ref));
        if (!isDeleted(blob)) return false;
        val request = DefaultUpdateAttributesRequest.builder()
            .removeAttribute(deletedAttribute)
            .build();
        val response = updateByRefIgnoreDeleted(ref, request);
        val event = new RestoreSoftDeletedBlobEvent(getDelegate(), clock, ref);
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
