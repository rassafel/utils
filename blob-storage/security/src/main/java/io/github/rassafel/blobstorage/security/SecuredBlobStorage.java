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


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.NotFoundBlobException;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.support.wrapper.DefaultDelegatedBlobStorage;

/**
 * Secured blob storage.
 * <p>
 * It uses a delegated blob storage and applies security policies to it.
 */
@Slf4j
public class SecuredBlobStorage extends DefaultDelegatedBlobStorage {
    public static final String DEFAULT_POLICY_ATTRIBUTE = "XS-Policy-Id";
    @NonNull
    private final String policyAttribute;
    @NonNull
    private final BlobPolicyHandler defaultHandler;
    private final Map<String, BlobPolicyHandler> handlers = new HashMap<>();

    public SecuredBlobStorage(BlobStorage delegate) {
        this(delegate, DEFAULT_POLICY_ATTRIBUTE, new StaticBlobPolicyHandler(true));
    }

    public SecuredBlobStorage(BlobStorage delegate, String policyAttribute) {
        this(delegate, policyAttribute, new StaticBlobPolicyHandler(true));
    }

    public SecuredBlobStorage(BlobStorage delegate, String policyAttribute, BlobPolicyHandler defaultHandler) {
        this(delegate, policyAttribute, defaultHandler, null);
    }

    public SecuredBlobStorage(BlobStorage delegate, String policyAttribute, BlobPolicyHandler defaultHandler, @Nullable Map<String, BlobPolicyHandler> handlers) {
        super(delegate);
        this.policyAttribute = policyAttribute;
        this.defaultHandler = defaultHandler;
        if (handlers != null) {
            this.handlers.putAll(handlers);
        }
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        var handler = getPolicyHandler(extractPolicyId(request));
        var updatedRequest = handler.beforeStore(request);
        var response = super.store(inputStream, updatedRequest);
        var blob = response.getStoredObject();
        return DefaultStoreBlobResponse.of(handler.postHandleBlob(blob));
    }

    @Override
    public boolean existsByRef(String ref) {
        return findByRef(ref).isPresent();
    }

    @Nullable
    @Override
    public StoredBlobObject getByRef(String ref) {
        var blob = super.getByRef(ref);
        if (blob == null) return null;
        var handler = getPolicyHandler(blob);
        if (handler.afterRetrieve(blob)) {
            return handler.postHandleBlob(blob);
        }
        return null;
    }

    @Override
    public Optional<StoredBlobObject> findByRef(String ref) {
        return super.findByRef(ref)
                .flatMap(blob -> {
                    var handler = getPolicyHandler(blob);
                    if (handler.afterRetrieve(blob)) {
                        return Optional.of(handler.postHandleBlob(blob));
                    }
                    return Optional.empty();
                });
    }

    @Override
    public boolean deleteByRef(String ref) {
        var blob = super.getByRef(ref);
        if (blob == null) return false;
        if (getPolicyHandler(blob).beforeDelete(blob)) {
            return super.deleteByRef(ref);
        }
        return false;
    }

    /**
     * Update blob object attributes by reference
     * <p>
     * Can override blob security policy
     *
     * @param ref     blob reference
     * @param request update attributes info. Remove attribute from blob if accepted value is null or blank
     * @return response with stored blob object
     * @throws NotFoundBlobException if blob not exists
     */
    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var blob = super.getByRef(ref);
        if (blob == null) {
            log.debug("Blob not found, ref: {}", ref);
            throw new NotFoundBlobException(ref);
        }
        var handler = getPolicyHandler(blob);
        var updateRequest = handler.beforeUpdate(blob, request);
        var response = super.updateByRef(ref, updateRequest);
        var updatedBlob = response.getStoredObject();
        return DefaultUpdateAttributesResponse.of(handler.postHandleBlob(updatedBlob));
    }

    protected BlobPolicyHandler getPolicyHandler(StoredBlobObject blob) {
        return getPolicyHandler(extractPolicyId(blob));
    }

    @Nullable
    protected String extractPolicyId(StoreBlobRequest request) {
        return request.getAttributes().get(policyAttribute);
    }

    @Nullable
    protected String extractPolicyId(StoredBlobObject blob) {
        return blob.getAttribute(policyAttribute);
    }

    protected BlobPolicyHandler getPolicyHandler(@Nullable String policyId) {
        var handler = handlers.get(policyId);
        if (handler == null) return defaultHandler;
        return handler;
    }
}
