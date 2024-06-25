package com.rassafel.io.storage.local;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.support.DefaultBlobObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;

public class LocalStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final Path localFile;

    protected LocalStoredBlobObject(BuilderImpl builder) {
        super(builder);
        Assert.notNull(builder.localFile, "local file path cannot be null");
        this.localFile = builder.localFile;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return Files.newInputStream(localFile, StandardOpenOption.READ);
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Builder builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    public interface Builder extends DefaultBlobObject.Builder {
        Builder localFile(Path localFile);

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
        Builder attributes(Map<String, String> attributes);

        @Override
        Builder attribute(String key, String value);

        @Override
        LocalStoredBlobObject build();
    }

    protected static class BuilderImpl extends DefaultBlobObject.BuilderImpl implements Builder {
        private Path localFile;

        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(LocalStoredBlobObject blob) {
            super(blob);
            localFile = blob.localFile;
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public Builder localFile(Path localFile) {
            this.localFile = localFile;
            return this;
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
        public Builder attributes(Map<String, String> attributes) {
            super.attributes(attributes);
            return this;
        }

        @Override
        public Builder attribute(String key, String value) {
            super.attribute(key, value);
            return this;
        }

        @Override
        public LocalStoredBlobObject build() {
            return new LocalStoredBlobObject(this);
        }
    }
}
