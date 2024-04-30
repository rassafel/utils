package com.rassafel.io.storage.mem;

import com.rassafel.io.storage.core.NotFoundBlobException;
import com.rassafel.io.storage.core.StoreBlobException;
import com.rassafel.io.storage.core.impl.AbstractBlobStorage;
import com.rassafel.io.storage.core.impl.KeyGenerator;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesResponse;
import lombok.val;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryBlobStorage extends AbstractBlobStorage {
    private final Map<String, InMemoryStoredBlobObject> blobs = new LinkedHashMap<>();
    private final Clock clock;

    public InMemoryBlobStorage(KeyGenerator keyGenerator, Clock clock) {
        super(keyGenerator);
        this.clock = clock;
    }

    @Override
    protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
        val now = LocalDateTime.now(clock);
        val builder = InMemoryStoredBlobObject.builder(request)
            .uploadedAt(now)
            .lastModifiedAt(now)
            .storedRef(blobKey);
        try {
            byte[] bytes;
            val requestSize = request.getSize();
            if (requestSize == null) {
                bytes = IOUtils.toByteArray(inputStream);
            } else {
                bytes = IOUtils.toByteArray(inputStream, Math.max(requestSize, 0));
            }
            builder.size(bytes.length)
                .bytes(bytes);
        } catch (IOException e) {
            val fileName = request.getOriginalName();
            log.error("Error saving blob to in memory storage, name: {}; blobKey: {}",
                fileName, blobKey, e);
            val message = String.format("Could not save blob %s.", fileName);
//          ToDo: add custom exception
            throw new StoreBlobException(message, e);
        }
        val object = builder.build();
        val prevValue = blobs.put(blobKey, object);
        if (prevValue != null) {
            log.debug("Replaced blob: {}", blobKey);
        }
        return new DefaultStoreBlobResponse(object);
    }

    @Override
    public InMemoryStoredBlobObject getByRef(String ref) {
        val blob = blobs.get(ref);
        if (blob == null) {
            log.debug("Blob not found, ref: {}", ref);
            return null;
        }
        log.debug("Blob found, ref: {}; name: {}", ref, blob.getOriginalName());
        return blob;
    }

    @Override
    public boolean deleteByRef(String ref) {
        val blob = blobs.remove(ref);
        if (blob == null) {
            log.debug("Blob not found, skip remove, ref: {}", ref);
            return false;
        }
        log.debug("Blob found, remove blob, ref: {}; name: {}", ref, blob.getOriginalName());
        return true;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val oldBlob = getByRef(ref);
//      ToDo: add custom exception
        if (oldBlob == null) throw new NotFoundBlobException(ref);
        val now = LocalDateTime.now(clock);
        val builder = oldBlob.toBuilder()
            .lastModifiedAt(now);
        request.getAttributes().forEach(builder::attribute);
        val blob = builder.build();
        blobs.put(ref, blob);
        return new DefaultUpdateAttributesResponse(blob);
    }
}
