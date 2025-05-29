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

package com.rassafel.blobstorage.core.query.impl;

import com.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

@Getter
public class DefaultUpdateAttributesRequest implements UpdateAttributesRequest {
    private final Map<String, String> attributes;

    protected DefaultUpdateAttributesRequest(AbstractBuilder<?, ?> builder) {
        this.attributes = new LinkedCaseInsensitiveMap<>(builder.attributes.size());
        this.attributes.putAll(builder.attributes);
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    @Override
    public Builder<?, ?> toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder<O extends DefaultUpdateAttributesRequest, B extends Builder<O, B>> extends UpdateAttributesRequest.Builder<O, B> {
    }

    protected abstract static class AbstractBuilder<O extends DefaultUpdateAttributesRequest, B extends AbstractBuilder<O, B>> implements Builder<O, B> {
        protected Map<String, String> attributes = new LinkedCaseInsensitiveMap<>();

        protected AbstractBuilder(DefaultUpdateAttributesRequest request) {
            request.getAttributes().forEach(this::attribute);
        }

        protected AbstractBuilder() {
        }

        @Override
        public B removeAttribute(String key) {
            return attribute(key, null);
        }

        @Override
        public B attribute(String key, @Nullable String value) {
            this.attributes.put(key, value);
            return self();
        }

        protected abstract B self();
    }

    protected static class BuilderImpl extends AbstractBuilder<DefaultUpdateAttributesRequest, BuilderImpl> {
        protected BuilderImpl(DefaultUpdateAttributesRequest request) {
            super(request);
        }

        protected BuilderImpl() {
            super();
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }

        @Override
        public DefaultUpdateAttributesRequest build() {
            return new DefaultUpdateAttributesRequest(this);
        }
    }
}
