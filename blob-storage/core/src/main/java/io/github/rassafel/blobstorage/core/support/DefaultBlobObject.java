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

package io.github.rassafel.blobstorage.core.support;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import io.github.rassafel.blobstorage.core.BlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;

@Getter
public class DefaultBlobObject implements BlobObject {
    private final String originalName;
    private final String storedRef;
    private final String contentType;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime lastModifiedAt;
    private final long size;
    private final Map<String, String> attributes = new LinkedCaseInsensitiveMap<>(1);

    protected DefaultBlobObject(AbstractBuilder<?, ?> builder) {
        this.originalName = builder.originalName;
        this.storedRef = builder.storedRef;
        this.contentType = builder.contentType;
        this.uploadedAt = builder.uploadedAt;
        this.lastModifiedAt = builder.lastModifiedAt;
        this.size = builder.size;
        this.attributes.putAll(builder.attributes);
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    public static Builder<DefaultBlobObject, ?> builder(BlobObject blobObject) {
        return new BuilderImpl(blobObject);
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends DefaultBlobObject, B extends Builder<O, B>> {
        B originalName(String originalName);

        B storedRef(String storedRef);

        B contentType(String contentType);

        B uploadedAt(LocalDateTime uploadedAt);

        B lastModifiedAt(LocalDateTime lastModifiedAt);

        B size(long size);

        B attributes(Map<String, String> attributes);

        B attribute(String key, @Nullable String value);

        O build();
    }

    protected abstract static class AbstractBuilder<O extends DefaultBlobObject, B extends Builder<O, B>>
            implements Builder<O, B> {
        protected final Map<String, String> attributes = new LinkedCaseInsensitiveMap<>(1);
        protected String originalName;
        protected String storedRef;
        protected String contentType;
        protected LocalDateTime uploadedAt;
        protected LocalDateTime lastModifiedAt;
        protected long size;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(BlobObject object) {
            originalName(object.getOriginalName());
            storedRef(object.getStoredRef());
            contentType(object.getContentType());
            size(object.getSize());
            attributes(object.getAttributes());
            uploadedAt(object.getUploadedAt());
            lastModifiedAt(object.getLastModifiedAt());
        }

        protected AbstractBuilder(StoreBlobRequest request) {
            originalName(request.getOriginalName());
            contentType(request.getContentType());
            size(ObjectUtils.defaultIfNull(request.getSize(), 0L));
            attributes(request.getAttributes());
        }

        @Override
        public B originalName(String originalName) {
            this.originalName = originalName;
            return self();
        }

        @Override
        public B storedRef(String storedRef) {
            this.storedRef = storedRef;
            return self();
        }

        @Override
        public B contentType(String contentType) {
            this.contentType = contentType;
            return self();
        }

        @Override
        public B uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return self();
        }

        @Override
        public B lastModifiedAt(LocalDateTime lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
            return self();
        }

        @Override
        public B size(long size) {
            this.size = size;
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

        @Override
        public B attribute(String key, @Nullable String value) {
            key = StringUtils.lowerCase(key);
            if (StringUtils.isBlank(value)) {
                this.attributes.remove(key);
            } else {
                this.attributes.put(key, value);
            }
            return self();
        }

        protected abstract B self();
    }

    protected static class BuilderImpl extends AbstractBuilder<DefaultBlobObject, BuilderImpl> {
        protected BuilderImpl() {
        }

        protected BuilderImpl(BlobObject object) {
            super(object);
        }

        public BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public DefaultBlobObject build() {
            return new DefaultBlobObject(this);
        }

        protected BuilderImpl self() {
            return this;
        }
    }
}
