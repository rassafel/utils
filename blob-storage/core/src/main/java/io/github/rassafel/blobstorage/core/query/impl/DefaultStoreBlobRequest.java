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

package io.github.rassafel.blobstorage.core.query.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;

@Getter
public class DefaultStoreBlobRequest implements StoreBlobRequest {
    private final String originalName;
    private final String contentType;
    private final Long size;
    private final Map<String, String> attributes = new LinkedCaseInsensitiveMap<>(1);

    protected DefaultStoreBlobRequest(BuilderImpl builder) {
        this.originalName = builder.originalName;
        this.contentType = builder.contentType;
        this.size = builder.size;
        this.attributes.putAll(builder.attributes);
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoredBlobObject obj) {
        return new BuilderImpl(obj);
    }

    @Override
    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends DefaultStoreBlobRequest, B extends Builder<O, B>>
            extends StoreBlobRequest.Builder<O, B> {
    }

    protected abstract static class AbstractBuilder<O extends DefaultStoreBlobRequest, B extends Builder<O, B>>
            implements Builder<O, B> {
        protected String originalName;

        protected String contentType;

        protected Long size;

        protected Map<String, String> attributes = new LinkedHashMap<>(1);

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(StoreBlobRequest request) {
            originalName(request.getOriginalName());
            contentType(request.getContentType());
            size(request.getSize());
            attributes(request.getAttributes());
        }

        protected AbstractBuilder(StoredBlobObject object) {
            originalName(object.getOriginalName());
            contentType(object.getContentType());
            size(object.getSize());
            attributes(object.getAttributes());
        }

        @Override
        public B originalName(String originalName) {
            this.originalName = originalName;
            return self();
        }

        @Override
        public B contentType(String contentType) {
            this.contentType = contentType;
            return self();
        }

        @Override
        public B size(@Nullable Long size) {
            this.size = size;
            return self();
        }

        @Override
        public B attribute(String key, String value) {
            this.attributes.put(key, value);
            return self();
        }

        @Override
        public B attributes(@Nullable Map<String, String> attributes) {
            this.attributes.clear();
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return self();
        }

        protected abstract B self();
    }

    protected static class BuilderImpl extends AbstractBuilder<DefaultStoreBlobRequest, BuilderImpl> {
        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        protected BuilderImpl(StoredBlobObject object) {
            super(object);
        }

        @Override
        public DefaultStoreBlobRequest build() {
            return new DefaultStoreBlobRequest(this);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
