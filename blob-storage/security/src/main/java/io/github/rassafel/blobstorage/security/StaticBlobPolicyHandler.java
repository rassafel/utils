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

package io.github.rassafel.blobstorage.security;


import lombok.RequiredArgsConstructor;

import io.github.rassafel.blobstorage.core.NotFoundBlobException;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;

@RequiredArgsConstructor
public class StaticBlobPolicyHandler implements BlobPolicyHandler {
    private final boolean allowed;

    @Override
    public StoreBlobRequest beforeStore(StoreBlobRequest request) {
        if (allowed) {
            return BlobPolicyHandler.super.beforeStore(request);
        }
        throw new StoreBlobDeniedException("Access denied");
    }

    @Override
    public boolean afterRetrieve(StoredBlobObject blob) {
        return allowed;
    }

    @Override
    public UpdateAttributesRequest beforeUpdate(StoredBlobObject blob, UpdateAttributesRequest request) {
        if (allowed) {
            return BlobPolicyHandler.super.beforeUpdate(blob, request);
        }
        throw new NotFoundBlobException(blob.getStoredRef());
    }

    @Override
    public boolean beforeDelete(StoredBlobObject blob) {
        return allowed;
    }
}
