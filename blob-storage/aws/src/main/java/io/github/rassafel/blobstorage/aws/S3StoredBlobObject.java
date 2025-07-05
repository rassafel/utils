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

package io.github.rassafel.blobstorage.aws;

import java.io.InputStream;

import lombok.NonNull;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;

import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.support.DefaultBlobObject;

public class S3StoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    @NonNull
    private final String bucket;
    @NonNull
    private final S3Client s3Client;

    protected S3StoredBlobObject(AbstractBuilder<?, ?> builder) {
        super(builder);
        Assert.notNull(this.getStoredRef(), "ref cannot be null");
        s3Client = builder.s3Client;
        bucket = builder.bucket;
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    @Override
    public InputStream toInputStream() {
        return s3Client.getObject(builder -> builder
                .bucket(bucket)
                .key(getStoredRef()), ResponseTransformer.toInputStream());
    }

    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends S3StoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.Builder<O, B> {
        B bucket(String bucket);

        B s3Client(S3Client s3Client);
    }

    protected abstract static class AbstractBuilder<O extends S3StoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.AbstractBuilder<O, B> implements Builder<O, B> {
        protected String bucket;
        protected S3Client s3Client;

        protected AbstractBuilder() {
            super();
        }

        protected AbstractBuilder(S3StoredBlobObject blob) {
            super(blob);
            bucket(blob.bucket);
            s3Client(blob.s3Client);
        }

        protected AbstractBuilder(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public B bucket(String bucket) {
            this.bucket = bucket;
            return self();
        }

        @Override
        public B s3Client(S3Client s3Client) {
            this.s3Client = s3Client;
            return self();
        }
    }

    protected static class BuilderImpl extends AbstractBuilder<S3StoredBlobObject, BuilderImpl> {
        protected BuilderImpl() {
        }

        protected BuilderImpl(S3StoredBlobObject blob) {
            super(blob);
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }

        @Override
        public S3StoredBlobObject build() {
            return new S3StoredBlobObject(this);
        }
    }
}
