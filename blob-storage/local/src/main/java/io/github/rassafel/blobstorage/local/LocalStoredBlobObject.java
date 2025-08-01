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

package io.github.rassafel.blobstorage.local;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import lombok.NonNull;

import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.support.DefaultBlobObject;

public class LocalStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    @NonNull
    private final Path localFile;

    protected LocalStoredBlobObject(BuilderImpl builder) {
        super(builder);
        this.localFile = builder.localFile;
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoreBlobRequest request) {
        return new BuilderImpl(request);
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return Files.newInputStream(localFile, StandardOpenOption.READ);
    }

    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends LocalStoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.Builder<O, B> {
        B localFile(Path localFile);
    }

    protected abstract static class AbstractBuilder<O extends LocalStoredBlobObject, B extends Builder<O, B>>
            extends DefaultBlobObject.AbstractBuilder<O, B> implements Builder<O, B> {
        protected Path localFile;

        protected AbstractBuilder() {
            super();
        }

        protected AbstractBuilder(LocalStoredBlobObject blob) {
            super(blob);
            localFile(blob.localFile);
        }

        protected AbstractBuilder(StoreBlobRequest request) {
            super(request);
        }

        @Override
        public B localFile(Path localFile) {
            this.localFile = localFile;
            return self();
        }
    }

    protected static class BuilderImpl extends AbstractBuilder<LocalStoredBlobObject, BuilderImpl> {
        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(LocalStoredBlobObject blob) {
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
        public LocalStoredBlobObject build() {
            return new LocalStoredBlobObject(this);
        }
    }
}
