package com.rassafel.io.storage.core.query.impl;

import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

/**
 *
 */
@AllArgsConstructor
@Getter
public class DefaultUpdateAttributesRequest implements UpdateAttributesRequest {
    private final Map<String, String> attributes;

    private DefaultUpdateAttributesRequest(BuilderImpl builder) {
        this.attributes = new LinkedCaseInsensitiveMap<>(builder.attributes.size());
        this.attributes.putAll(builder.attributes);
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public interface Builder extends UpdateAttributesRequest.Builder {
        @Override
        Builder removeAttribute(String key);

        @Override
        Builder attribute(String key, @Nullable String value);

        @Override
        DefaultUpdateAttributesRequest build();
    }

    private static class BuilderImpl implements Builder {
        private Map<String, String> attributes = new LinkedCaseInsensitiveMap<>();

        private BuilderImpl(DefaultUpdateAttributesRequest request) {
            request.getAttributes().forEach(this::attribute);
        }

        private BuilderImpl() {
        }

        @Override
        public Builder removeAttribute(String key) {
            return attribute(key, null);
        }

        @Override
        public Builder attribute(String key, @Nullable String value) {
            this.attributes.put(key, value);
            return this;
        }

        @Override
        public DefaultUpdateAttributesRequest build() {
            return new DefaultUpdateAttributesRequest(this);
        }
    }
}
