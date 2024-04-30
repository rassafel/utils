package com.rassafel.io.storage.core;

import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.util.Optional;

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
     * @param request update attributes info.
     *                Remove attribute from blob if accepted value is null or blank
     * @return response with stored blob object
     * @throws {@link NotFoundBlobException} if blob not exists
     */
    UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request);
}
