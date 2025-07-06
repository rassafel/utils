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

package io.github.rassafel.blobstorage.core.support.wrapper;

import java.io.InputStream;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;

/**
 * Delegated BlobStorage
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedBlobStorage implements BlobStorage {
    @NonNull
    private final BlobStorage delegate;

    protected BlobStorage getDelegate() {
        return delegate;
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        return getDelegate().store(inputStream, request);
    }

    @Override
    public boolean existsByRef(String ref) {
        return getDelegate().existsByRef(ref);
    }

    @Nullable
    @Override
    public StoredBlobObject getByRef(String ref) {
        return getDelegate().getByRef(ref);
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return getDelegate().findByRef(ref);
    }

    @Override
    public boolean deleteByRef(String ref) {
        return getDelegate().deleteByRef(ref);
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        return getDelegate().updateByRef(ref, request);
    }
}
