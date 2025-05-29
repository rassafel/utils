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

package com.rassafel.blobstorage.event.type;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.event.BlobEvent;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Default blob event
 */
public class DefaultBlobEvent implements BlobEvent {
    private final BlobStorage storage;
    private final LocalDateTime timestamp;

    public DefaultBlobEvent(BlobStorage storage) {
        this(storage, LocalDateTime.now());
    }

    public DefaultBlobEvent(BlobStorage storage, Clock clock) {
        this(storage, LocalDateTime.now(clock));
    }

    public DefaultBlobEvent(BlobStorage storage, LocalDateTime timestamp) {
        this.storage = storage;
        this.timestamp = timestamp;
    }

    @Override
    public BlobStorage getStorage() {
        return storage;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
