/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rassafel.blobstorage.event.support;


import com.rassafel.blobstorage.event.BlobEvent;
import com.rassafel.blobstorage.event.BlobListener;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;

public class BlobListenerComposite implements GenericBlobListener {
    private static final String NULL_LISTENER_MESSAGE = "listener cannot be null";
    private final Collection<GenericBlobListener> listeners = new LinkedHashSet<>();

    private static GenericBlobListener wrapListener(BlobListener<?> listener) {
        if (listener instanceof GenericBlobListener genericListener) {
            return genericListener;
        }
        return new GenericBlobListenerAdapter(listener);
    }

    private static BlobListener<?> unwrapListener(BlobListener<?> listener) {
        if (listener instanceof GenericBlobListenerAdapter adapter) {
            return adapter.getDelegate();
        }
        return listener;
    }

    public void addBlobListener(BlobListener<?> listener) {
        Assert.notNull(listener, NULL_LISTENER_MESSAGE);
        var wrappedListener = wrapListener(listener);
        addBlobListener(wrappedListener);
    }

    public void addBlobListener(GenericBlobListener listener) {
        Assert.notNull(listener, NULL_LISTENER_MESSAGE);
        listeners.add(listener);
    }

    public void removeBlobListener(BlobListener<?> listener) {
        Assert.notNull(listener, NULL_LISTENER_MESSAGE);
        listeners.removeIf(l -> {
            var unwrappedListener = unwrapListener(l);
            return unwrappedListener == listener;
        });
    }

    @Override
    public boolean supportsEventType(Class<? extends BlobEvent> clazz) {
        for (var listener : listeners) {
            if (listener.supportsEventType(clazz)) return true;
        }
        return false;
    }

    @Override
    public void onBlobEvent(BlobEvent event) {
        var clazz = event.getClass();
        for (var listener : listeners) {
            if (listener.supportsEventType(clazz)) {
                listener.onBlobEvent(event);
            }
        }
    }
}
