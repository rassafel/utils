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

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.lang.Nullable;

import io.github.rassafel.blobstorage.core.BlobObject;

/**
 * Delegation BlobObject
 */
public class DefaultDelegationBlobObject implements BlobObject {
    private final BlobObject delegate;

    public DefaultDelegationBlobObject(BlobObject delegate) {
        this.delegate = delegate;
    }

    protected BlobObject getDelegate() {
        return delegate;
    }

    @Override
    public String getOriginalName() {
        return getDelegate().getOriginalName();
    }

    @Override
    public String getStoredRef() {
        return getDelegate().getStoredRef();
    }

    @Override
    public String getContentType() {
        return getDelegate().getContentType();
    }

    @Override
    public LocalDateTime getUploadedAt() {
        return getDelegate().getUploadedAt();
    }

    @Override
    public LocalDateTime getLastModifiedAt() {
        return getDelegate().getLastModifiedAt();
    }

    @Override
    public long getSize() {
        return getDelegate().getSize();
    }

    @Override
    public Map<String, String> getAttributes() {
        return getDelegate().getAttributes();
    }

    @Nullable
    @Override
    public String getAttribute(String key) {
        return getDelegate().getAttribute(key);
    }
}
