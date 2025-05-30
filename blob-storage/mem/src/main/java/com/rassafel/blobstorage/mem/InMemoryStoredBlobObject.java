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

package com.rassafel.blobstorage.mem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.support.DefaultBlobObject;

public class InMemoryStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final byte[] bytes;

    protected InMemoryStoredBlobObject(AbstractBuilder<?, ?> builder) {
        super(builder);
        if (builder.bytes != null) {
            this.bytes = Arrays.copyOf(builder.bytes, builder.bytes.length);
        } else {
            this.bytes = new byte[]{};
        }
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    @Override
    public InputStream toInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends InMemoryStoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.Builder<O, B> {
        B bytes(byte[] bytes);
    }

    protected abstract static class AbstractBuilder<O extends InMemoryStoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.AbstractBuilder<O, B> implements Builder<O, B> {
        protected byte[] bytes;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(InMemoryStoredBlobObject object) {
            super(object);
            if (object.bytes != null) {
                bytes(Arrays.copyOf(object.bytes, object.bytes.length));
            }
        }

        protected AbstractBuilder(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public B bytes(byte[] bytes) {
            this.bytes = bytes;
            return self();
        }
    }

    protected static class BuilderImpl extends AbstractBuilder<InMemoryStoredBlobObject, BuilderImpl> {
        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(InMemoryStoredBlobObject object) {
            super(object);
        }

        protected BuilderImpl(StoreBlobRequest request) {
            super(request);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }

        @Override
        public InMemoryStoredBlobObject build() {
            return new InMemoryStoredBlobObject(this);
        }
    }
}
