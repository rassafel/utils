package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Convert BlobEvent to Spring ApplicationEvent
 */
@RequiredArgsConstructor
public class BlobEventToApplicationEventListener implements GenericBlobListener {
    private final ApplicationEventPublisher publisher;

    @Override
    public void onBlobEvent(BlobEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public boolean supportsEventType(Class<? extends BlobEvent> clazz) {
        return BlobEvent.class.isAssignableFrom(clazz);
    }
}
