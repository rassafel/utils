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

package com.rassafel.blobstorage.core.query;

import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * Request of store a blob.
 */
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

    Builder<?, ?> toBuilder();

    interface Builder<O extends StoreBlobRequest, B extends Builder<O, B>> {
        B originalName(String originalName);

        B contentType(String contentType);

        B size(@Nullable Long size);

        B attribute(String key, String value);

        B attributes(@Nullable Map<String, String> attributes);

        O build();
    }
}
