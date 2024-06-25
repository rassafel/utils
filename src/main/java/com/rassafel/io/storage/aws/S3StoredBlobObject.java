package com.rassafel.io.storage.aws;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.support.DefaultBlobObject;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

public class S3StoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final String bucket;
    private final S3Client s3Client;

    protected S3StoredBlobObject(BuilderImpl builder) {
        super(builder);
        Assert.notNull(builder.s3Client, "s3 client cannot be null");
        Assert.notNull(builder.bucket, "bucket cannot be null");
        Assert.notNull(this.getStoredRef(), "ref cannot be null");
        s3Client = builder.s3Client;
        bucket = builder.bucket;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return s3Client.getObject(builder -> builder
            .bucket(bucket)
            .key(getStoredRef()), ResponseTransformer.toInputStream());
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
        Builder bucket(String bucket);

        Builder s3Client(S3Client s3Client);

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
        S3StoredBlobObject build();
    }

    protected static class BuilderImpl extends DefaultBlobObject.BuilderImpl implements Builder {
        private String blobKey;
        private String bucket;
        private S3Client s3Client;

        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(S3StoredBlobObject blob) {
            super(blob);
            bucket = blob.bucket;
            s3Client = blob.s3Client;
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        @Override
        public Builder s3Client(S3Client s3Client) {
            this.s3Client = s3Client;
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
        public S3StoredBlobObject build() {
            return new S3StoredBlobObject(this);
        }
    }
}
