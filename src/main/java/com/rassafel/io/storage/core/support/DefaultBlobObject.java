package com.rassafel.io.storage.core.support;

import com.rassafel.io.storage.core.BlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class DefaultBlobObject implements BlobObject {
    private final String originalName;
    private final String storedRef;
    private final String contentType;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime lastModifiedAt;
    private final long size;
    private final Map<String, String> attributes;

    protected DefaultBlobObject(BuilderImpl builder) {
        this.originalName = builder.originalName;
        this.storedRef = builder.storedRef;
        this.contentType = builder.contentType;
        this.uploadedAt = builder.uploadedAt;
        this.lastModifiedAt = builder.lastModifiedAt;
        this.size = builder.size;
        this.attributes = new HashMap<>(builder.attributes);
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Builder builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    public static Builder builder(BlobObject blobObject) {
        return new BuilderImpl(blobObject);
    }

    public interface Builder {
        Builder originalName(String originalName);

        Builder storedRef(String storedRef);

        Builder contentType(String contentType);

        Builder uploadedAt(LocalDateTime uploadedAt);

        Builder lastModifiedAt(LocalDateTime lastModifiedAt);

        Builder size(long size);

        Builder attributes(Map<String, String> attributes);

        Builder attribute(String key, @Nullable String value);

        DefaultBlobObject build();
    }

    protected static class BuilderImpl implements Builder {
        private String originalName;
        private String storedRef;
        private String contentType;
        private LocalDateTime uploadedAt;
        private LocalDateTime lastModifiedAt;
        private long size;
        private Map<String, String> attributes = new LinkedHashMap<>();

        protected BuilderImpl() {
        }

        protected BuilderImpl(BlobObject object) {
            originalName(object.getOriginalName());
            storedRef(object.getStoredRef());
            contentType(object.getContentType());
            size(object.getSize());
            attributes(object.getAttributes());
            uploadedAt(object.getUploadedAt());
            lastModifiedAt(object.getLastModifiedAt());
        }

        protected BuilderImpl(StoreBlobRequest request) {
            originalName(request.getOriginalName());
            contentType(request.getContentType());
            size(ObjectUtils.defaultIfNull(request.getSize(), 0L));
            attributes(request.getAttributes());
        }

        @Override
        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        @Override
        public Builder storedRef(String storedRef) {
            this.storedRef = storedRef;
            return this;
        }

        @Override
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        @Override
        public Builder uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        @Override
        public Builder lastModifiedAt(LocalDateTime lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
            return this;
        }

        @Override
        public Builder size(long size) {
            this.size = size;
            return this;
        }

        @Override
        public Builder attributes(Map<String, String> attributes) {
            if (attributes == null) {
                this.attributes = new LinkedHashMap<>();
            } else {
                this.attributes = new LinkedHashMap<>(attributes.size());
                attributes.forEach(this::attribute);
            }
            return this;
        }

        @Override
        public Builder attribute(String key, @Nullable String value) {
            if (StringUtils.isBlank(value)) {
                this.attributes.remove(key);
            } else {
                this.attributes.put(key, value);
            }
            return this;
        }

        @Override
        public DefaultBlobObject build() {
            return new DefaultBlobObject(this);
        }
    }
}
