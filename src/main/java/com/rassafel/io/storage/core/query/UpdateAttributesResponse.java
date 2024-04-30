package com.rassafel.io.storage.core.query;

import com.rassafel.io.storage.core.StoredBlobObject;

/**
 * Blob object wrapper for update attributes response
 */
public interface UpdateAttributesResponse {
    /**
     * Stored blob object
     *
     * @return stored blob object
     */
    StoredBlobObject getStoredObject();
}
