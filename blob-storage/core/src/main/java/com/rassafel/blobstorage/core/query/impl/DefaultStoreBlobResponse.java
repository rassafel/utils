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

package com.rassafel.blobstorage.core.query.impl;

import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.StoreBlobResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DefaultStoreBlobResponse implements StoreBlobResponse {
    private final StoredBlobObject storedObject;

    public static DefaultStoreBlobResponse of(StoredBlobObject storedObject) {
        return new DefaultStoreBlobResponse(storedObject);
    }
}
