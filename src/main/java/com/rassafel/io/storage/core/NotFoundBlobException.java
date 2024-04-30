package com.rassafel.io.storage.core;

import lombok.Getter;

/**
 * Not found blob exception
 */
@Getter
public class NotFoundBlobException extends StoreBlobException {
    private String ref;

    public NotFoundBlobException(String ref) {
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, String message) {
        super(message);
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, String message, Throwable cause) {
        super(message, cause);
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, Throwable cause) {
        super(cause);
        this.ref = ref;
    }
}
