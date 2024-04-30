package com.rassafel.io.storage.core;

import org.springframework.lang.Nullable;

/**
 * Blob storage locator by storage name
 * <p>
 * Default storage name is null
 */
public interface BlobStorageLocator {
    /**
     * Find blob storage by name
     *
     * @param storageName storage name
     * @return Blob storage by name
     */
    BlobStorage findStorage(@Nullable String storageName);

    /**
     * Get default blob storage
     *
     * @return default blob storage
     */
    BlobStorage getDefaultStorage();
}
