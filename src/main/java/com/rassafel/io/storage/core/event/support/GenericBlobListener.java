package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import com.rassafel.io.storage.core.event.BlobListener;

/**
 * Generic blob listener.
 * Accepts all events filtered with {@link #supportsEventType(Class)}
 */
public interface GenericBlobListener extends BlobListener<BlobEvent> {
    boolean supportsEventType(Class<? extends BlobEvent> clazz);
}
