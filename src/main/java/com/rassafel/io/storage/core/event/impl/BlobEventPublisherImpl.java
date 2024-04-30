package com.rassafel.io.storage.core.event.impl;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.event.BlobEvent;
import com.rassafel.io.storage.core.event.BlobListener;
import com.rassafel.io.storage.core.event.BlobEventPublisher;
import com.rassafel.io.storage.core.event.support.GenericBlobListener;
import com.rassafel.io.storage.core.event.support.GenericBlobListenerAdapter;
import lombok.val;

import java.util.Collection;
import java.util.LinkedHashSet;

public class BlobEventPublisherImpl implements BlobEventPublisher {
    private final Collection<BlobListener<?>> blobListeners = new LinkedHashSet<>();

    @Override
    public void addBlobListener(BlobListener<?> listener) {
        Assert.notNull(listener, "listener cannot be null");
        blobListeners.add(listener);
    }

    @Override
    public void removeBlobListener(BlobListener<?> listener) {
        blobListeners.remove(listener);
    }

    @Override
    public void publish(BlobEvent event) {
        for (val listener : blobListeners) {
            val genericListener = listener instanceof GenericBlobListener ?
                (GenericBlobListener) listener : new GenericBlobListenerAdapter(listener);
            if (genericListener.supportsEventType(event.getClass())) {
                genericListener.onBlobEvent(event);
            }
        }
    }
}
