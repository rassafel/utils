package com.rassafel.io.storage.core.event;

/**
 * Blob event publisher
 */
public interface BlobEventPublisher {
    /**
     * Attach listener to publisher
     *
     * @param listener blob event listener
     */
    void addBlobListener(BlobListener<?> listener);

    /**
     * Detach listener from publisher
     *
     * @param listener blob event listener
     */
    void removeBlobListener(BlobListener<?> listener);

    /**
     * Publish event. Send event to listeners.
     *
     * @param event blob event
     */
    void publish(BlobEvent event);
}
