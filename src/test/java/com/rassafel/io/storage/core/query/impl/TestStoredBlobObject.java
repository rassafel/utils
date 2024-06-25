package com.rassafel.io.storage.core.query.impl;

import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.support.DefaultBlobObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

public class TestStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final InputStream inputStream;

    protected TestStoredBlobObject(BuilderImpl builder) {
        super(builder);
        inputStream = builder.inputStream;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return inputStream;
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Builder builder(StoredBlobObject object) {
        return new BuilderImpl(object);
    }

    public interface Builder extends DefaultBlobObject.Builder {

        Builder inputStream(InputStream inputStream);

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
        TestStoredBlobObject build();
    }

    protected static class BuilderImpl extends DefaultBlobObject.BuilderImpl implements Builder {
        private InputStream inputStream;

        protected BuilderImpl() {
        }

        public BuilderImpl(StoredBlobObject object) {
            super(object);
            try {
                this.inputStream = object.toInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        @Override
        public TestStoredBlobObject build() {
            return new TestStoredBlobObject(this);
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
    }
}
