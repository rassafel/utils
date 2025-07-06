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


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.support.wrapper.DefaultDelegationStoredBlobObject;

@RequiredArgsConstructor
public class TestBlobPolicyHandler implements BlobPolicyHandler {
    private final Map<String, String> additionalAttributes = new LinkedCaseInsensitiveMap<>();

    public TestBlobPolicyHandler(@Nullable Map<String, String> additionalAttributes) {
        if (additionalAttributes != null) {
            this.additionalAttributes.putAll(additionalAttributes);
        }
    }

    @Override
    public StoreBlobRequest beforeStore(StoreBlobRequest request) {
        var builder = request.toBuilder();
        additionalAttributes.forEach(builder::attribute);
        return builder.build();
    }

    @Override
    public StoredBlobObject postHandleBlob(StoredBlobObject blob) {
        return new HideAttributesStoredBlobObject(blob, additionalAttributes.keySet());
    }

    @Override
    public UpdateAttributesRequest beforeUpdate(StoredBlobObject blob, UpdateAttributesRequest request) {
        var builder = request.toBuilder();
        for (var attribute : additionalAttributes.keySet()) {
            builder.removeAttribute(attribute);
        }
        return builder.build();
    }

    private static final class HideAttributesStoredBlobObject extends DefaultDelegationStoredBlobObject {
        private final Collection<String> ignoreAttributes = new LinkedHashSet<>();

        public HideAttributesStoredBlobObject(StoredBlobObject delegate, Collection<String> ignoreAttributes) {
            super(delegate);
            this.ignoreAttributes.addAll(ignoreAttributes);
        }

        @Nullable
        @Override
        public String getAttribute(String key) {
            if (ignoreAttributes.contains(key)) return null;
            return super.getAttribute(key);
        }

        @Override
        public Map<String, String> getAttributes() {
            var attributes = new LinkedCaseInsensitiveMap<String>();
            attributes.putAll(super.getAttributes());
            for (var attribute : ignoreAttributes) {
                attributes.remove(attribute);
            }
            return Collections.unmodifiableMap(attributes);
        }
    }
}
