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

package io.github.rassafel.blobstorage.core;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.lang.Nullable;

import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;

/**
 * The blob storage abstraction (store, find, update, delete)
 */
public interface BlobStorage {
    /**
     * Store input stream to storage
     *
     * @param inputStream blob stream to store
     * @param request     store info
     * @return response with stored blob object
     * @throws StoreBlobException if input stream problem
     */
    StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request);

    /**
     * Check is blob exists in storage
     *
     * @param ref blob reference
     * @return true if exists else false
     */
    default boolean existsByRef(String ref) {
        return findByRef(ref).isPresent();
    }

    /**
     * Get blob object by reference
     *
     * @param ref blob reference
     * @return stored blog object if exists, else null
     */
    @Nullable
    StoredBlobObject getByRef(String ref);

    /**
     * Get blob object by reference
     *
     * @param ref blob reference
     * @return stored blog object if exists, else empty
     */
    default Optional<StoredBlobObject> findByRef(String ref) {
        return Optional.ofNullable(getByRef(ref));
    }

    /**
     * Delete blob object by reference
     *
     * @param ref blob reference
     * @return true if deleted else false (not exists, etc.)
     */
    boolean deleteByRef(String ref);

    /**
     * Update blob object attributes by reference
     *
     * @param ref     blob reference
     * @param request update attributes info. Remove attribute from blob if accepted value is null or blank
     * @return response with stored blob object
     * @throws NotFoundBlobException if blob not exists
     */
    UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request);
}
