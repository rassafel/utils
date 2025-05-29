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

package com.rassafel.blobstorage.event.impl;

import com.rassafel.blobstorage.event.BlobEvent;
import com.rassafel.blobstorage.event.BlobEventPublisher;
import com.rassafel.blobstorage.event.BlobListener;
import com.rassafel.blobstorage.event.support.GenericBlobListener;
import com.rassafel.blobstorage.event.support.GenericBlobListenerAdapter;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;

public class BlobEventPublisherImpl implements BlobEventPublisher {
    private final Collection<GenericBlobListener> listeners = new LinkedHashSet<>();

    @Override
    public void addBlobListener(BlobListener<?> listener) {
        Assert.notNull(listener, "listener cannot be null");
        var genericListener = listener instanceof GenericBlobListener ?
            (GenericBlobListener) listener : new GenericBlobListenerAdapter(listener);
        listeners.add(genericListener);
    }

    @Override
    public void removeBlobListener(BlobListener<?> listener) {
        listeners.remove(listener);
    }

    @Override
    public void publish(BlobEvent event) {
        for (var listener : listeners) {
            if (listener.supportsEventType(event.getClass())) {
                listener.onBlobEvent(event);
            }
        }
    }
}
