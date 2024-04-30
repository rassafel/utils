package com.rassafel.io.storage.core;

import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * The stored blob object
 */
public interface StoredBlobObject extends BlobObject {
    /**
     * @return the underlying implementation object (based on the provider)
     */
    @Nullable
    Object getImplementation();

    /**
     * @return input stream of blob
     * @throws IOException
     */
    @Nullable
    InputStream toInputStream() throws IOException;
}
