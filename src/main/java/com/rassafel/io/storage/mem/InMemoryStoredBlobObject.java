package com.rassafel.io.storage.mem;

import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.support.DefaultBlobObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Getter
@Slf4j
public class InMemoryStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final byte[] bytes;

    private InMemoryStoredBlobObject(BuilderImpl builder) {
        super(builder);
        if (builder.bytes != null) {
            this.bytes = Arrays.copyOf(builder.bytes, builder.bytes.length);
        } else {
            this.bytes = new byte[]{};
        }
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Builder builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    @Override
    public Object getImplementation() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder extends DefaultBlobObject.Builder {
        Builder bytes(byte[] bytes);

        @Override
        Builder originalName(String originalName);

        @Override
        Builder storedRef(String storedRef);

        @Override
        Builder contentType(String contentType);

        @Override
        Builder uploadedAt(LocalDateTime uploadedAt);

        @Override
        Builder lastModifiedAt(LocalDateTime lastModifiedAt);

        @Override
        Builder size(long size);

        @Override
        Builder attributes(Map<String, String> metadata);

        @Override
        Builder attribute(String key, @Nullable String value);

        Builder removeAttribute(String key);

        @Override
        InMemoryStoredBlobObject build();
    }

    protected static class BuilderImpl extends DefaultBlobObject.BuilderImpl implements Builder {
        private byte[] bytes;

        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(InMemoryStoredBlobObject object) {
            super(object);
            if (object.bytes != null) {
                this.bytes = Arrays.copyOf(object.bytes, object.bytes.length);
            }
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public Builder bytes(byte[] bytes) {
            this.bytes = bytes;
            return this;
        }

        @Override
        public InMemoryStoredBlobObject build() {
            return new InMemoryStoredBlobObject(this);
        }

        @Override
        public Builder originalName(String originalName) {
            super.originalName(originalName);
            return this;
        }

        @Override
        public Builder storedRef(String storedRef) {
            super.storedRef(storedRef);
            return this;
        }

        @Override
        public Builder contentType(String contentType) {
            super.contentType(contentType);
            return this;
        }

        @Override
        public Builder uploadedAt(LocalDateTime uploadedAt) {
            super.uploadedAt(uploadedAt);
            return this;
        }

        @Override
        public Builder lastModifiedAt(LocalDateTime lastModifiedAt) {
            super.lastModifiedAt(lastModifiedAt);
            return this;
        }

        @Override
        public Builder size(long size) {
            super.size(size);
            return this;
        }

        @Override
        public Builder attributes(Map<String, String> metadata) {
            super.attributes(metadata);
            return this;
        }

        @Override
        public Builder removeAttribute(String key) {
            return attribute(key, null);
        }

        @Override
        public Builder attribute(String key, @Nullable String value) {
            super.attribute(key, value);
            return this;
        }
    }
}
