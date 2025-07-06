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

package io.github.rassafel.blobstorage.mem;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.NotFoundBlobException;
import io.github.rassafel.blobstorage.core.StoreBlobException;
import io.github.rassafel.blobstorage.core.impl.KeyGenerator;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.util.FileTypeUtils;

/**
 * In-memory implementation of BlobStorage. Stores blobs in a map with blob keys as the keys and InMemoryStoredBlobObject objects as the values.
 */
@Slf4j
@RequiredArgsConstructor
public class InMemoryBlobStorage implements BlobStorage {
    private final Map<String, InMemoryStoredBlobObject> blobs = new ConcurrentHashMap<>();
    @NonNull
    private final KeyGenerator keyGenerator;
    @NonNull
    private final Clock clock;

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        var originalName = request.getOriginalName();
        var blobKey = generateBlobKey(originalName);
        var builder = request.toBuilder();
        var contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            var type = FileTypeUtils.getMimeTypeFromName(originalName);
            builder.contentType(type.toString());
        }
        return store(blobKey, inputStream, builder.build());
    }

    protected String generateBlobKey(String originalName) {
        var blobKey = KeyGenerator.SEPARATOR + keyGenerator.createKey(originalName);
        var extension = FilenameUtils.getExtension(originalName);
        if (StringUtils.isNotBlank(extension)) {
            return blobKey + "." + extension;
        }
        return blobKey;
    }

    protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
        var now = LocalDateTime.now(clock);
        var builder = InMemoryStoredBlobObject.builder(request)
                .uploadedAt(now)
                .lastModifiedAt(now)
                .storedRef(blobKey);
        try {
            byte[] bytes;
            var requestSize = request.getSize();
            if (requestSize == null) {
                bytes = IOUtils.toByteArray(inputStream);
            } else {
                bytes = IOUtils.toByteArray(inputStream, Math.max(requestSize, 0));
            }
            builder.size(bytes.length).bytes(bytes);
        } catch (IOException e) {
            var fileName = request.getOriginalName();
            log.error("Error saving blob to in memory storage, name: {}; blobKey: {}",
                    fileName, blobKey, e);
            var message = String.format("Could not save blob %s.", fileName);
//          ToDo: add custom exception
            throw new StoreBlobException(message, e);
        }
        var object = builder.build();
        var prevValue = blobs.put(blobKey, object);
        if (prevValue != null) {
            log.debug("Replaced blob: {}", blobKey);
        }
        return DefaultStoreBlobResponse.of(object);
    }

    @Nullable
    @Override
    public InMemoryStoredBlobObject getByRef(String ref) {
        var blob = blobs.get(ref);
        if (blob == null) {
            log.debug("Blob not found, ref: {}", ref);
            return null;
        }
        log.debug("Blob found, ref: {}; name: {}", ref, blob.getOriginalName());
        return blob;
    }

    @Override
    public boolean deleteByRef(String ref) {
        var blob = blobs.remove(ref);
        if (blob == null) {
            log.debug("Blob not found, skip remove, ref: {}", ref);
            return false;
        }
        log.debug("Blob found, remove blob, ref: {}; name: {}", ref, blob.getOriginalName());
        return true;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var result = blobs.compute(ref, (r, blob) -> {
            if (blob == null) {
                log.debug("Blob not found, ref: {}", ref);
//              ToDo: add custom exception
                throw new NotFoundBlobException(ref);
            }
            log.debug("Blob found, ref: {}; name: {}", ref, blob.getOriginalName());
            var builder = blob.toBuilder()
                    .lastModifiedAt(LocalDateTime.now(clock));
            request.getAttributes().forEach(builder::attribute);
            return builder.build();
        });
        return DefaultUpdateAttributesResponse.of(result);
    }
}
