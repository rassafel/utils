package com.rassafel.io.storage.core;

/**
 * Base blob exception.
 */
public class StoreBlobException extends RuntimeException {
    public StoreBlobException() {
    }

    public StoreBlobException(String message) {
        super(message);
    }

    public StoreBlobException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreBlobException(Throwable cause) {
        super(cause);
    }
}
