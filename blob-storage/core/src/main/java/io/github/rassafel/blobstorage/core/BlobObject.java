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

package io.github.rassafel.blobstorage.core;

import java.time.LocalDateTime;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The blob object which contains the uploaded-data's information saved in backend
 */
public interface BlobObject {
    /**
     * The original name of store request
     *
     * @return original name
     */
    String getOriginalName();

    /**
     * The final stored physical file name in backend (the stored blob name must be unique)
     * <p>
     * example 1: /2024/04/30/87cc8dae-1587-43f1-914b-d71550e394b6.jpg
     * <p>
     * example 2: fs:/2024/04/30/87cc8dae-1587-43f1-914b-d71550e394b6.jpg
     * <p>
     * example 3: s3:/2024/04/30/87cc8dae-1587-43f1-914b-d71550e394b6.jpg
     *
     * @return stored ref
     */
    String getStoredRef();

    /**
     * The value of http header content type ( when download in the future , we will set it back to http response)
     *
     * @return http header Content-Type
     */
    String getContentType();

    /**
     * The date time when the blob is uploaded
     *
     * @return the uploaded date time
     */
    LocalDateTime getUploadedAt();

    /**
     * The date time when the blob is last updated
     *
     * @return the uploaded date time
     */
    LocalDateTime getLastModifiedAt();

    /**
     * The size of uploaded blob
     *
     * @return size
     */
    long getSize();

    /**
     * The customized attributes for the uploaded blob
     *
     * @return attributes
     */
    Map<String, String> getAttributes();

    /**
     * The customized attribute for the uploaded blob
     *
     * @return attribute
     */
    @Nullable
    String getAttribute(String key);
}
