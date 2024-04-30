package com.rassafel.io.storage.core.event;

/**
 *
 */
public interface BlobEventPublisher {
    void publish(BlobEvent event);
}
