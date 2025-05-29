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

package com.rassafel.blobstorage.core.util;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.core.NotFoundBlobException;
import com.rassafel.blobstorage.core.StoreBlobException;
import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.query.StoreBlobResponse;
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
@Slf4j
public class BlobStorageUtils {
    /**
     * Copy a blob from one storage to another.
     *
     * @param ref         the reference of the blob to be copied
     * @param from        the source BlobStorage where the blob is stored
     * @param to          the destination BlobStorage where the blob will be copied
     * @return the response containing the stored blob object in the destination
     */
    public static StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to) {
        return copy(ref, from, to, builder -> {
        });
    }

    /**
     * Copies a blob from one storage to another using the provided mutator.
     *
     * @param ref         Reference of the blob to be copied.
     * @param from        Source BlobStorage where the blob is stored.
     * @param to          Destination BlobStorage where the blob will be copied.
     * @param mutator     Consumer that modifies a StoreBlobRequest.Builder before building it.
     * @return The response containing the stored blob object in the destination.
     */
    public static StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to,
                                         Consumer<StoreBlobRequest.Builder<?, ?>> mutator) {
        return copy(ref, from, to, (StoredBlobObject obj) -> {
            var builder = DefaultStoreBlobRequest.builder(obj);
            mutator.accept(builder);
            return builder.build();
        });
    }

    /**
     * Copies a blob from one storage to another using the provided mapper function.
     *
     * @param ref         Reference of the blob to be copied.
     * @param from        Source BlobStorage where the blob is stored.
     * @param to          Destination BlobStorage where the blob will be copied.
     * @param mapper      Function that maps the StoredBlobObject to a StoreBlobRequest for storage in the destination.
     * @return The response containing the stored blob object in the destination.
     */
    public static StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to,
                                         Function<StoredBlobObject, StoreBlobRequest> mapper) {
        try {
            return copyInner(ref, from, to, mapper);
        } catch (IOException e) {
            log.debug("Fail move blob, ref: {}; from: {}; to: {}", ref, from, to, e);
            throw new StoreBlobException("Fail move blob", e);
        }
    }

    /**
     * Moves a blob from one storage to another.
     *
     * @param ref the reference of the blob to be moved.
     * @param from the source BlobStorage where the blob is stored.
     * @param to the destination BlobStorage where the blob will be moved.
     * @return the response containing the moved blob object in the destination.
     */
    public static StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to) {
        return move(ref, from, to, builder -> {
        });
    }

    /**
     * Moves a blob from one storage to another, using the provided mutator to customize the store request.
     *
     * @param ref         reference of the blob to be moved
     * @param from        source BlobStorage where the blob is stored
     * @param to          destination BlobStorage where the blob will be moved
     * @param mutator     consumer that modifies a StoreBlobRequest.Builder before building it
     * @return the response containing the moved blob object in the destination
     */
    public static StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to,
                                         Consumer<StoreBlobRequest.Builder<?, ?>> mutator) {
        return move(ref, from, to, (StoredBlobObject obj) -> {
            var builder = DefaultStoreBlobRequest.builder(obj);
            mutator.accept(builder);
            return builder.build();
        });
    }

    /**
     * Moves a blob from one storage to another.
     *
     * @param ref reference of the blob to be moved
     * @param from source BlobStorage where the blob is stored
     * @param to destination BlobStorage where the blob will be moved
     * @param mapper function that maps the StoredBlobObject to a StoreBlobRequest for storage in the destination
     * @return response containing the moved blob object in the destination
     */
    public static StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to,
                                         Function<StoredBlobObject, StoreBlobRequest> mapper) {
        try {
            var copy = copyInner(ref, from, to, mapper);
            from.deleteByRef(ref);
            return copy;
        } catch (IOException e) {
            log.debug("Fail move blob, ref: {}; from: {}; to: {}", ref, from, to, e);
            throw new StoreBlobException("Fail move blob", e);
        }
    }

    private static StoreBlobResponse copyInner(String ref, BlobStorage from, BlobStorage to,
                                               Function<StoredBlobObject, StoreBlobRequest> mapper) throws IOException {
        Assert.notNull(from, "from cannot be null");
        Assert.notNull(to, "to cannot be null");
        var obj = from.findByRef(ref)
            .orElseThrow(() -> new NotFoundBlobException(ref));
        try (InputStream inputStream = obj.toInputStream()) {
            var in = inputStream;
            if (in == null) in = new ByteArrayInputStream(new byte[0]);
            return to.store(in, mapper.apply(obj));
        }
    }
}
