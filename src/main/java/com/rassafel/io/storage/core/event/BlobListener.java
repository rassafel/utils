package com.rassafel.io.storage.core.event;

/**
 *
 */
public interface BlobListener<E extends BlobEvent> {
    void onBlobEvent(E event);
}
