package com.rassafel.io.storage.core;

import java.time.LocalDateTime;
import java.util.Map;

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
    String getAttribute(String key);
}
