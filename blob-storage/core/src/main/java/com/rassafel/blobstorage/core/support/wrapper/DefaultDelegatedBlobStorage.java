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

package com.rassafel.blobstorage.core.support.wrapper;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.query.StoreBlobResponse;
import com.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import com.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.Optional;

/**
 * Delegated BlobStorage
 */
@Slf4j
public class DefaultDelegatedBlobStorage implements BlobStorage {
    private final BlobStorage delegate;

    public DefaultDelegatedBlobStorage(BlobStorage delegate) {
        Assert.notNull(delegate, "delegate cannot be null");
        this.delegate = delegate;
    }

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
