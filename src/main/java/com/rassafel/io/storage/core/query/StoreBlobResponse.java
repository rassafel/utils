package com.rassafel.io.storage.core.query;

import com.rassafel.io.storage.core.StoredBlobObject;

/**
 * Blob object wrapper for store response
 */
public interface StoreBlobResponse {
    /**
     * Stored blob object
     *
     * @return stored blob object
     */
    StoredBlobObject getStoredObject();
}
