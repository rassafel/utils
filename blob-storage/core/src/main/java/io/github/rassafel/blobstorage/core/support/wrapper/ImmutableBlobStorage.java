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

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;

public class ImmutableBlobStorage extends DefaultDelegatedBlobStorage {
    public ImmutableBlobStorage(BlobStorage delegate) {
        super(delegate);
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public boolean deleteByRef(String ref) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        throw new RuntimeException("unsupported");
    }
}
