package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import com.rassafel.io.storage.core.event.BlobEventPublisher;

/**
 * Event publisher to void
 */
public class NoOpBlobEventPublisher implements BlobEventPublisher {
    private static final NoOpBlobEventPublisher INSTANCE = new NoOpBlobEventPublisher();

    public static NoOpBlobEventPublisher getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(BlobEvent event) {
    }
}
