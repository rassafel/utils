package com.rassafel.io.storage.core.query;

import org.springframework.lang.Nullable;

import java.util.Map;

public interface StoreBlobRequest {

    /**
     * The original blob name of store request
     *
     * @return original blob name
     */
    String getOriginalName();

    /**
     * The value of http header content type ( when download in the future , we will set it back to http response)
     *
     * @return http header Content-Type
     */
    String getContentType();

    /**
     * @return blob size, unknown if null
     */
    @Nullable
    Long getSize();

    /**
     * The customized attributes for the uploaded blob
     *
     * @return attributes
     */
    Map<String, String> getAttributes();

    Builder toBuilder();

    interface Builder {
        Builder originalName(String originalName);

        Builder contentType(String contentType);

        Builder size(@Nullable Long size);

        Builder attribute(String key, String value);

        Builder attributes(Map<String, String> attributes);

        StoreBlobRequest build();
    }
}
