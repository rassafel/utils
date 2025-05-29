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

package com.rassafel.blobstorage.test;


import com.rassafel.blobstorage.core.StoredBlobObject;
import com.rassafel.blobstorage.core.support.DefaultBlobObject;

import java.io.IOException;
import java.io.InputStream;

public class TestStoredBlobObject extends DefaultBlobObject implements StoredBlobObject {
    private final InputStream inputStream;

    protected TestStoredBlobObject(BuilderImpl builder) {
        super(builder);
        inputStream = builder.inputStream;
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    public static Builder<?, ?> builder(StoredBlobObject object) {
        return new BuilderImpl(object);
    }

    @Override
    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public InputStream toInputStream() {
        return inputStream;
    }

    public interface Builder<O extends TestStoredBlobObject, B extends Builder<O, B>> extends DefaultBlobObject.Builder<O, B> {
        B inputStream(InputStream inputStream);
    }

    protected static class BuilderImpl
        extends DefaultBlobObject.AbstractBuilder<TestStoredBlobObject, BuilderImpl>
        implements Builder<TestStoredBlobObject, BuilderImpl> {
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
        public BuilderImpl inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return self();
        }

        @Override
        public TestStoredBlobObject build() {
            return new TestStoredBlobObject(this);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
