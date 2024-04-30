package com.rassafel.io.storage.core.query.impl;

import com.rassafel.commons.builder.CopyableBuilder;
import com.rassafel.commons.builder.ToCopyableBuilder;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class DefaultStoreBlobRequest implements StoreBlobRequest,
    ToCopyableBuilder<DefaultStoreBlobRequest.Builder, DefaultStoreBlobRequest> {
    private final String originalName;
    private final String contentType;
    private final Long size;
    private final Map<String, String> attributes;

    private DefaultStoreBlobRequest(BuilderImpl builder) {
        this.originalName = builder.originalName;
        this.contentType = builder.contentType;
        this.size = builder.size;
        this.attributes = new LinkedHashMap<>(builder.attributes);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Builder builder(StoredBlobObject obj) {
        return new BuilderImpl(obj);
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder extends StoreBlobRequest.Builder, CopyableBuilder<Builder, DefaultStoreBlobRequest> {
        @Override
        DefaultStoreBlobRequest build();
    }

    private static class BuilderImpl implements Builder {
        private String originalName;

        private String contentType;

        private Long size;

        private Map<String, String> attributes = new LinkedHashMap<>();

        public BuilderImpl() {
        }

        private BuilderImpl(StoreBlobRequest request) {
            originalName(request.getOriginalName());
            contentType(request.getContentType());
            size(request.getSize());
            attributes(request.getAttributes());
        }

        private BuilderImpl(StoredBlobObject object) {
            originalName(object.getOriginalName());
            contentType(object.getContentType());
            size(object.getSize());
            attributes(object.getAttributes());
        }

        @Override
        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        @Override
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        @Override
        public Builder size(@Nullable Long size) {
            this.size = size;
            return this;
        }

        @Override
        public Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        @Override
        public Builder attributes(Map<String, String> attributes) {
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }
            this.attributes = new LinkedHashMap<>(attributes);
            return this;
        }

        @Override
        public DefaultStoreBlobRequest build() {
            return new DefaultStoreBlobRequest(this);
        }
    }
}
