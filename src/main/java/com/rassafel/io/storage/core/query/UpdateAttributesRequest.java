package com.rassafel.io.storage.core.query;

import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Update attributes blob request
 */
public interface UpdateAttributesRequest {
    /**
     * The customized attributes. Remove blob attribute if value is null.
     * Change only accepted attributes.
     *
     * @return attributes
     */
    Map<String, String> getAttributes();

    Builder toBuilder();

    interface Builder {
        Builder removeAttribute(String key);

        Builder attribute(String key, @Nullable String value);

        UpdateAttributesRequest build();
    }
}
