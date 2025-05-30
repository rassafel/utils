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

/**
 * No-op implementation of {@link BlobEventPublisher}. Send events to void.
 */
public class NoOpBlobEventPublisher implements BlobEventPublisher {
    private static final NoOpBlobEventPublisher INSTANCE = new NoOpBlobEventPublisher();

    public static NoOpBlobEventPublisher getInstance() {
        return INSTANCE;
    }

    @Override
    public void addBlobListener(BlobListener<?> listener) {
    }

    @Override
    public void removeBlobListener(BlobListener<?> listener) {
    }

    @Override
    public void publish(BlobEvent event) {
    }
}
