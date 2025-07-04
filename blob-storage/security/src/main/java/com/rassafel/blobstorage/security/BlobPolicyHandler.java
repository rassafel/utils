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

package com.rassafel.blobstorage.security;


import com.rassafel.blobstorage.core.NotFoundBlobException;
import com.rassafel.blobstorage.core.StoreBlobException;
import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.query.UpdateAttributesRequest;

/**
 * Policy handler interface for handling security policies.
 */
public interface BlobPolicyHandler {
    /**
     * Before store a blob object.
     *
     * @param request the store request.
     * @return modified store request or source store request.
     * @throws StoreBlobException       if the store operation is failed.
     * @throws StoreBlobDeniedException if the store operation is denied.
     */
    default StoreBlobRequest beforeStore(StoreBlobRequest request) {
        return request;
    }

    /**
     * Use for hide attributes of stored blob object.
     *
     * @param blob the stored blob object.
     * @return modified stored blob object or source stored blob object.
     */
    default StoredBlobObject postHandleBlob(StoredBlobObject blob) {
        return blob;
    }

    /**
     * Before store a blob object.
     *
     * @param blob the retrieved blob object.
     * @return true if the blob access is allowed, false otherwise.
     */
    default boolean afterRetrieve(StoredBlobObject blob) {
        return true;
    }

    /**
     * Before update a blob object.
     *
     * @param blob    the blob object to be updated.
     * @param request the update request.
     * @return modified update request or source update request.
     * @throws NotFoundBlobException if the blob is not found or not accessible.
     */
    default UpdateAttributesRequest beforeUpdate(StoredBlobObject blob, UpdateAttributesRequest request) {
        return request;
    }

    /**
     * Before delete a blob object.
     *
     * @param blob the blob to be deleted.
     * @return true if the blob access is allowed, false otherwise.
     */
    default boolean beforeDelete(StoredBlobObject blob) {
        return true;
    }
}
